import json
from copy import deepcopy
from functools import partial


def escape_xpath(xpath):
    if xpath:
        return xpath.replace('"', "&quot;").replace("'", "&apos;")


def update_vals(m, f):
    return {k: f(v) for (k, v) in m.items()}


def walk(inner, outer, form):
    ret = form.copy()  # shallow copy
    if ret.get('type') == 'array':
        ret['items'] = inner(ret['items'])
    elif ret.get('type') == 'object':
        ret['properties'] = update_vals(ret['properties'], inner)
    return outer(ret)


def postwalk(f, form):
    return walk(partial(postwalk, f), f, form)


def prewalk(f, form):
    return walk(partial(prewalk, f), lambda x: x, f(form))


def gen_import_xslt(spec):
    spec_type = spec.get("type")
    tag_name = spec.get('xsl:tag name')
    value_of_select = spec.get('xsl:value-of select')
    for_each_select = spec.get('xsl:for-each select')

    if spec_type == 'object':
        print("""<%s>""" % tag_name)
        for prop_spec in spec.get('properties').values():
            gen_import_xslt(prop_spec)
        print("""</%s>""" % tag_name)
    elif spec_type == 'array':
        item_spec = spec.get('items')
        if for_each_select:
            print("""<xsl:for-each select="%s">""" % escape_xpath(for_each_select))
            gen_import_xslt(item_spec)
            print("""</xsl:for-each>""")
    else:
        print("""<%s>""" % tag_name)
        if value_of_select:
            print("""<xsl:value-of select="%s" />""" % escape_xpath(value_of_select))
        print("""</%s>""" % tag_name)


def gen_import(spec):
    print("""<?xml version="1.0" encoding="UTF-8"?>""")
    print("""<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" """)
    for k, v in spec.get('namespaces').items():
        print("""  xmlns:%s="%s" """ % (k, v))
    print(""">""")
    print("""<xsl:strip-space elements="*"/>""")
    print("""<xsl:output indent="yes"/>""")
    print("""<xsl:template match="/">""")
    gen_import_xslt(spec)
    print("""</xsl:template>""")
    print("""</xsl:stylesheet>""")


def is_ref(schema):
    return '$ref' in schema


def get_ref_schema(defs, schema):
    assert '$ref' in schema
    assert schema['$ref'].startswith('#/$defs/')
    def_name = schema['$ref'].split('#/$defs/')[1]
    assert def_name
    return defs.get(def_name, {})


def insert_def(defs, schema):
    if is_ref(schema):
        return deepcopy(get_ref_schema(defs, schema))
    else:
        return schema


def inline_defs(schema):
    """
    replace any node group reference with the actual node_group
    """
    defs = schema.get('$defs')
    if defs:
        schema = prewalk(partial(insert_def, defs), schema)
        del schema['$defs']
    return schema


def full_xpaths_step(schema):
    """
    Push down xpaths and generate full_xpath annotations.

    :param schema:
    :return:
    """

    # Set full_xpath if needed and possible
    full_xpath = schema.get("full_xpath")
    xpath = schema.get("xpath")
    parent_xpath = schema.get("parent_xpath")
    if schema.get("type") == "array":
        full_xpath = parent_xpath
        schema['full_xpath'] = full_xpath
    elif xpath and xpath.startswith("/"):
        full_xpath = xpath
        schema['full_xpath'] = full_xpath
    elif not full_xpath and xpath and parent_xpath:
        full_xpath = parent_xpath + "/" + xpath
        schema['full_xpath'] = full_xpath

    # Set parent_xpath if possible
    if full_xpath:
        schema_type = schema.get('type')
        if schema_type == 'object':
            valueChild = schema.get('valueChild')
            if valueChild:
                full_xpath = full_xpath + "/" + valueChild
            for prop_name in schema['properties'].keys():
                schema['properties'][prop_name]['parent_xpath'] = full_xpath
        elif schema_type == 'array':
            schema['items']['parent_xpath'] = full_xpath

    # Clear parent_xpath if present (clean up)
    if parent_xpath:
        del schema['parent_xpath']

    return schema


def full_xpaths(schema):
    """
    Experimental analysis to resolve full_xpath based on schema tree and xpath annotations.
    """
    schema['full_xpath'] = schema['xpath']
    return prewalk(full_xpaths_step, schema)


def xslt_extract_step(schema):
    """
    add xslt extraction annotations

    :param schema:
    :return:
    """

    type = schema.get('type')
    full_xpath = schema.get('full_xpath')
    valueChild = schema.get('valueChild')
    attributes = schema.get('attributes')

    if type == 'object':
        for prop_name, prop_schema in schema.get('properties').items():
            prop_schema['xsl:tag name'] = prop_name
    elif type == 'array':
        items = schema.get('items')
        items_xpath = items.get('xpath')
        if items_xpath:
            schema['items']['xsl:tag name'] = schema.get('xsl:tag name')
            schema['xsl:for-each select'] = full_xpath + "/" + items_xpath
    else:

        if full_xpath:
            value_of_select = full_xpath

            if valueChild:
                value_of_select = value_of_select + "/" + valueChild

            if attributes is None:
                schema['xsl:value-of select'] = value_of_select
            elif attributes.get('text'):
                # treat text attr as blessed for reading
                schema['xsl:value-of select'] = value_of_select
            elif len(attributes) == 1:
                # Single attribute clear for import
                k = list(attributes.keys())[0]
                schema['xsl:value-of select'] = value_of_select + "/@" + k
            else:
                raise Exception("Unable to infer for multiple attributes cases")

    return schema


def xslt_extract(schema):
    """
    Experimental analysis to resolve full_xpath based on schema tree and xpath annotations.
    """
    schema['xsl:tag name'] = 'root'
    assert schema.get('type') == 'object', "Only tested with object root, found %s" % schema.get('type')
    return prewalk(xslt_extract_step, schema)


def generate(filename):
    """
    Generate XSL file to extract values from XML.

    Requires annotations from full_xpaths and xslt_extract analysis.

    :param filename:
    :param self:
    :param spec:
    :return:
    """
    file = open(filename, 'r')
    payload = file.read()
    schema = json.loads(payload)
    schema = inline_defs(schema)
    schema = full_xpaths(schema)
    schema = xslt_extract(schema)
    gen_import(schema)


def generate_to_file(in_file, out_file):
    import contextlib

    with open(out_file, "w") as o:
        with contextlib.redirect_stdout(o):
            generate(in_file)
