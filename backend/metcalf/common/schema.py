from functools import partial

from metcalf.common.xmlutils import SpecialKeys


def walk(inner, outer, form):
    ret = form.copy()  # shallow copy
    if ret.get('type') == 'array':
        ret['items'] = inner(ret['items'])
    elif ret.get('type') == 'object':
        ret['properties'] = {k: inner(v) for (k, v) in ret['properties'].items()}
    return outer(ret)


def postwalk(f, form):
    return walk(partial(postwalk, f), f, form)


def prewalk(f, form):
    return walk(partial(prewalk, f), lambda x: x, f(form))


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
        return get_ref_schema(defs, schema)
    else:
        return schema


def inline_defs(schema):
    defs = schema.get('$defs')
    if defs:
        schema = prewalk(partial(insert_def, defs), schema)
        del schema['$defs']
    return schema


def remove_comment(schema):
    if isinstance(schema, dict):
        schema.pop(SpecialKeys.comment, None)
    return schema
