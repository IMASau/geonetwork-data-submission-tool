import json


def to_json(x):
    if isinstance(x, str):
        return json.loads(x)
    # Else hope it's already json
    return x
