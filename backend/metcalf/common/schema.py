from functools import partial


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
