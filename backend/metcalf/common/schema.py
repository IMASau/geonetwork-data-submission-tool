from functools import partial

from metcalf.common.xmlutils import SpecialKeys


def select_keys(m, ks):
    return {k: m[k] for k in ks if k in m}


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
