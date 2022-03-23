import json
import copy


def get_data_at_path(data, path):
    data = copy.deepcopy(data)
    path = copy.deepcopy(path)

    if len(path) > 0:
        key = path.pop(0)
        if type(key) is list:
            return [get_data_at_path(d, key) for d in data]
        else:
            return get_data_at_path(data[key], path)
    else:
        return data

def set_data_at_path(data, path, value):
    data = copy.deepcopy(data)
    path = copy.deepcopy(path)

    key = path.pop(0)
    if type(key) is list:
        while len(data) < len(value):
            data.append([] if type(key[0]) is list else {})
        data = [set_data_at_path(data[i], key, v) for i, v in enumerate(value)]
    else:
        if len(path) > 0:
            if key not in data.keys(): 
                data[key] = [] if type(path[0]) is list else {}
            data[key] = set_data_at_path(data[key], path, value)
        else:
            data[key] = value
    return data

def migrate_data(input, output, migration):
    src = migration['src']
    dst = migration['dst']
    value = get_data_at_path(input, src)

    if 'fn' in migration.keys():
        fn = functions[migration['fn']]
        value = fn(value)

    return set_data_at_path(output, dst, value)

def clear_empty_keys(data):
    data = copy.deepcopy(data)
    if type(data) is dict:
        data = dict((k, clear_empty_keys(v)) for k, v in data.items())
        data = dict((k, v) for k, v in data.items() if not (v == None or v == {} or v == [] or v == ""))
    if type(data) is list:
        data = [clear_empty_keys(v) for v in data]
        data = [v for v in data if not (v == None or v == {} or v == [] or v == "")]
    return data

input_filepath = input('Please input the filepath to the data you wish to migrate: ')
input_data = json.loads(open(input_filepath, 'r').read())

migrations_filepath = input('Please input the filepath to the list of migrations you wish to make (leave blank for default): ')
if len(migrations_filepath) == 0:
    migrations_filepath = 'data_migration_tool/migrations.json'
migrations = json.loads(open(migrations_filepath, 'r').read())

output_filepath = input('Please input the filepath to where you wish to output the migrated data (leave blank for default): ')
if len(output_filepath) == 0:
    output_filepath = 'data_migration_tool/output.json'

output_data = {}

for migration in migrations:
    output_data = migrate_data(input_data, output_data, migration)

output_data = clear_empty_keys(output_data)

open(output_filepath, 'wt').write(json.dumps(output_data))