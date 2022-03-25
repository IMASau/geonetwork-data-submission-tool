import json
import copy

def todo(value):
    return None

def my_capitalize(value):
    return value.capitalize()

def flora(value):
    # TODO: treats every taxon keyword as flora (when it could be fauna)
    return [{
        # 'uri': TODO,
        # 'acceptedNameUsage': TODO,
        'label': v,
        # 'altLabel': TODO
    } for v in value]

def fauna(value):
    # TODO: treats every taxon keyword as fauna (when it could be flora)
    return [{
        'label': v,
        # 'uri': TODO,
        # 'broader_uri': TODO,
        # 'has_children': TODO,
        # 'breadcrumb': TODO
    } for v in value]

def parse_num(value):
    try:
        v = float(value)
        return v
    except:
        return value

def exists(value):
    return value != None and value != ""

def full_address_line(value):
    return f"{value['deliveryPoint']} {value['city']} {value['administrativeArea']} {value['postalCode']} {value['country']}"

def name(value):
    return f"{value['givenName']} {value['familyName']}"

def party_type(value):
    if value['givenName'] == 'a_not_applicable' or value['givenName'] == '':
        return 'organisation'
    else:
        return 'person'

def other_constraints(value):
    return [
            "TERN services are provided on an \"as-is\" and \"as available\" basis. Users use any TERN services at their discretion and risk.\n            They will be solely responsible for any damage or loss whatsoever that results from such use including use of any data obtained through TERN and any analysis performed using the TERN infrastructure.\n            <br />Web links to and from external, third party websites should not be construed as implying any relationships with and/or endorsement of the external site or its content by TERN.\n            <br /><br />Please advise any work or publications that use this data via the online form at https://www.tern.org.au/research-publications/#reporting",
            "Please cite this dataset as {Author} ({PublicationYear}). {Title}. {Version, as appropriate}. Terrestrial Ecosystem Research Network. Dataset. {Identifier}.\n            "
        ]

functions = {
    'todo': todo,
    'capitalize': my_capitalize,
    'flora': flora,
    'fauna': fauna,
    'parseNum': parse_num,
    'exists': exists,
    'fullAddressLine': full_address_line,
    'name': name,
    'partyType': party_type,
    'otherConstraints': other_constraints,
    'true': lambda value : True,
    'join': lambda value: "\n".join(value)
}

def get_data_at_path(data, path):
    data = copy.deepcopy(data)
    path = copy.deepcopy(path)

    if len(path) > 0:
        key = path.pop(0)
        if type(key) is list:
            return [get_data_at_path(d, key) for d in data]
        else:
            if not (type(data) is list and key >= len(data)):
                return get_data_at_path(data[key], path)
            else:
                return None
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


def depth(path):
    if (type(path[-1]) is list):
        return depth(path[-1]) + 1
    else:
        return 0

def apply(value, fn, depth):
    value = copy.deepcopy(value)
    if depth == 0:
        return fn(value)
    else:
        return [apply(v, fn, depth - 1) for v in value]

def migrate_data(input, output, migration):
    src = migration['src']
    dst = migration['dst']
    value = get_data_at_path(input, src)

    if 'fn' in migration.keys():
        fn = functions[migration['fn']]
        value = apply(value, fn, depth(dst))

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

input_filepath = input('Please input the filepath to the data you wish to migrate(leave blank for xgz.json): ')
if len(input_filepath) == 0:
    input_filepath = 'data_migration_tool/xgz.json'
input_data = json.loads(open(input_filepath, 'r').read())

migrations_filepath = input('Please input the filepath to the list of migrations you wish to make (leave blank for default): ')
if len(migrations_filepath) == 0:
    migrations_filepath = 'data_migration_tool/migrations.json'
migrations = json.loads(open(migrations_filepath, 'r').read())

output_filepath = input('Please input the filepath to where you wish to output the migrated data (leave blank for default): ')
if len(output_filepath) == 0:
    output_filepath = 'data_migration_tool/output.json'

template_filepath = input('Please input the filepath to the output template data (leave blank for default): ')
if len(template_filepath) == 0:
    template_filepath = 'data_migration_tool/template.json'

output_data = json.loads(open(template_filepath, 'r').read())

for migration in migrations:
    output_data = migrate_data(input_data, output_data, migration)

output_data = clear_empty_keys(output_data)

open(output_filepath, 'wt').write(json.dumps(output_data))