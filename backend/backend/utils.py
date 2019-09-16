import json


def to_json(x):
    if isinstance(x, str):
        return json.loads(x)
    # Else hope it's already json
    return x


def get_exception_message(e):
    return e.args[0] if len(e.args) == 1 else ''
