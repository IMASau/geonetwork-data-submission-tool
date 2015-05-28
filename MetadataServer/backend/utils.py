import json


def to_json(x):
    if isinstance(x, basestring):
        return json.loads(x)
    # Else hope it's already json
    return x
