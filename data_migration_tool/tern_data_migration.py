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

def protocol(value):
    protocols = [
        {'label': 'HTTP',
         'value': 'WWW:DOWNLOAD-1.0-http--download'},
        {'label': 'OGC Web Coverage Service (WCS)',
         'value': 'OGC:WCS-1.1.0-http-get-capabilities'},
        {'label': 'OGC Web Map Service (WMS)',
         'value': 'OGC:WMS-1.3.0-http-get-map'},
        {'label': 'OGC Web Feature Service (WFS)',
         'value': 'OGC:WFS-1.1.0-http-get-capabilities'},
         {'label': 'OPeNDAP',
         'value': 'WWW:LINK-1.0-http--opendap'},
        {'label': 'FTP',
         'value': 'FTP'},
        {'label': 'Other/unknown',
         'value': 'WWW:DOWNLOAD-1.0-http--download'}
    ]
    return next(v for v in protocols if v['value'] == value)

def role(value):
    roles = [
        {'UUID': 'a37cc120-9920-4495-9a2f-698e225b5902',
        'Identifier': 'author',
        'Description': 'Party who authored the resource'},
        {'UUID': 'cc22ca92-a323-42fa-8e01-1503f0edf6b9',
        'Identifier': 'coAuthor',
        'Description': 'Party who jointly authors the resource'},
        {'UUID': 'a2d57717-48fb-4675-95dd-4be8f9d585d6',
        'Identifier': 'collaborator',
        'Description': 'Party who assists with the generation of the resource other than the principal investigator'},
        {'UUID': 'b91ddbe5-584e-46ff-a242-1c7c67b836e3',
        'Identifier': 'contributor',
        'Description': 'Party contributing to the resource'},
        {'UUID': '3373d310-f065-4ece-a61b-9bb04bd1df27',
        'Identifier': 'custodian',
        'Description': 'Party that accepts accountability and responsibility for the resource and ensures appropriate care and maintenance of the resource'},
        {'UUID': 'abd843f7-9d47-4a69-b9bc-3544202488fe',
        'Identifier': 'distributor',
        'Description': 'Party who distributes the resource'},
        {'UUID': '370e8b34-d7ce-42fc-904f-05e263789389',
        'Identifier': 'editor',
        'Description': 'Party who reviewed or modified the resource to improve the content'},
        {'UUID': '06213565-8aff-4c98-9ae3-4dd1023a2cdc',
        'Identifier': 'funder',
        'Description': 'Party providing monetary support for the resource'},
        {'UUID': '2961f936-74cf-4192-95dc-959e8dae7189',
        'Identifier': 'mediator',
        'Description': 'A class of entity that mediates access to the resource and for whom the resource is intended or useful'},
        {'UUID': '6cd5bbc6-463d-4850-9ad4-2353cb9451f5',
        'Identifier': 'originator',
        'Description': 'Party who created the resource'},
        {'UUID': '0e75b54c-0cff-4753-a66a-c359f604689d',
        'Identifier': 'owner',
        'Description': 'Party that owns the resource'},
        {'UUID': '6b20a462-bc67-46c3-bdcb-b558f0127fe2',
        'Identifier': 'principalInvestigator',
        'Description': 'Key party responsible for gathering information and conducting research'},
        {'UUID': 'c3429513-50aa-4288-b919-cdeb816815a7',
        'Identifier': 'processor',
        'Description': 'Party who has processed the data in a manner such that the resource has been modified'},
        {'UUID': '1359d456-c428-49f1-8c8e-c46ebff53a10',
        'Identifier': 'publisher',
        'Description': 'Party who published the resource'},
        {'UUID': 'b25e217a-ed48-4d10-831e-298975f6cedf',
        'Identifier': 'resourceProvider',
        'Description': 'Party that supplies the resource'},
        {'UUID': '028232f0-36c8-4ff6-aef4-ec0c424b7887',
        'Identifier': 'rightsHolder',
        'Description': 'Party owning or managing rights over the resource'},
        {'UUID': '8211c24f-e1be-4a2d-962e-856304fa53de',
        'Identifier': 'sponsor',
        'Description': 'Party who speaks for the resource'},
        {'UUID': 'a9199aa5-26e2-4951-af7b-3132118d7569',
        'Identifier': 'stakeholder',
        'Description': 'Party who has an interest in the resource or the use of the resource'},
        {'UUID': '4122989f-f824-4d4a-8a29-10bd3541c17e',
        'Identifier': 'user',
        'Description': 'Party who uses the resource'}
    ]
    return next(v for v in roles if v['Identifier'] == value)

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
    'join': lambda value: "\n".join(value),
    'protocol': protocol,
    'role': role
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