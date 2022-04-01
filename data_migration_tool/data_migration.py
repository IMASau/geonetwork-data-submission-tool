import copy
import uuid
import re

def parse_num(value):
    try:
        v = float(value)
        return v
    except:
        return value

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
    try:
        return next(v for v in protocols if v['value'] == value)
    except:
        return {'value': value}

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
    try:
        return next(v for v in roles if v['Identifier'] == value) if isinstance(value, str) else value
    except:
        return {'Identifier': value}

def parameter(value):
    return [{
        'label': v.get('platform_term'),
        'description': v.get('platform_termDefinition'),
        'uri': v.get('platform_vocabularyTermURL') if v.get('platform_vocabularyTermURL') != 'http://linkeddata.tern.org.au/XXX' else None,
        'source': v.get('platform_vocabularyVersion')
    } for v in value]

def platform(value):
    return [{
        'parameter': {
            'label': v.get('longName_term'),
            'description': v.get('longName_termDefinition'),
            'uri': v.get('longName_vocabularyTermURL') if v.get('longName_vocabularyTermURL') != 'http://linkeddata.tern.org.au/XXX' else None,
            'source': v.get('longName_vocabularyVersion')
        },
        'unit': {
            'label': v.get('unit_term'),
            'uri': v.get('unit_vocabularyTermURL') if v.get('unit_vocabularyTermURL') != 'http://linkeddata.tern.org.au/XXX' else None,
            'source': v.get('unit_vocabularyVersion')
        },
        'uri' : str(uuid.uuid4())
    } for v in value] if value != None else None

def instrument(value):
    return [{
        'serial': v.get('serialNumber'),
        'label': v.get('instrument_term'),
        'description': v.get('instrument_termDefinition'),
        'uri': v.get('instrument_vocabularyTermURL') if v.get('instrument_vocabularyTermURL') != 'http://linkeddata.tern.org.au/XXX' else None,
        'source': v.get('instrument_vocabularyVersion')
    } for v in value] if value != None else None

def keywordsTheme(value):
    return [{
        # 'breadcrumb': TODO,
        'label': f'https://gcmd.earthdata.nasa.gov/kms/concept/{v}',
        'uri': f'https://gcmd.earthdata.nasa.gov/kms/concept/{v}',
        # 'broader_concept': TODO
    } if isinstance(v, str) else v for v in value] if value != None else None

def keywordsThemeAnzsrc(value):
    return [{
        # 'breadcrumb': TODO,
        'label': v,
        'uri': v,
        # 'broader_concept': TODO
    } if isinstance(v, str) else v for v in value] if value != None else None

def keywordsHorizontal(value):
    return {
        # 'breadcrumb': TODO,
        'label': value[0]['prefLabel'],
        'uri': value[0]['uri'],
        # 'broader_concept': TODO
    } if (value != None and len(value) > 0) else None

def keywordsTemporal(value):
    return keywordsHorizontal(value)

def distributor(value):
    return {
        'is_dissolved': 'false',
        'name': 'TERN Ecosystem Processes - UQ Long Pocket',
        'full_address_line': 'Building 1019, 80 Meiers Rd, Indooroopilly, QLD, Australia, 4068',
        'postcode': '4068',
        'address_region': 'QLD',
        'address_locality': 'Indooroopilly',
        'date_modified': '2021-05-14T05:18:27.281Z',
        'street_address': 'Building 1019, 80 Meiers Rd',
        'date_created': '2021-05-14T03:33:48.260Z',
        'uri': 'https://w3id.org/tern/resources/8f2acf9f-3cf2-48c7-b911-ed1b1113932e',
        'display_name': 'TERN Ecosystem Processes - UQ Long Pocket',
        'site_uri': 'https://w3id.org/tern/resources/fa56a1ed-ec38-4294-90ae-ab203a25d5ad',
        'country': 'Australia'
    }

def vertical_crs(value):
    crs = [
        {
            'identifier': 'EPSG::5714',
            'name': 'MSL height',
            'label': 'Altitude (height above mean sea level)'
        },
        {
            'identifier': 'EPSG::5715',
            'name': 'MSL depth',
            'label': 'Depth (distance below mean sea level)'
        }
    ]

    try:
        return next(v for v in crs if v['identifier'] == value)
    except:
        return {
            'identifier': value,
            'name': value,
            'label': value
        }

def creative_commons(value):
    if value == None:
        return None

    constraints = {
        'by': {
            'label': 'Creative Commons Attribution 4.0 International License',
            'value': 'CC-BY'
        },
        'by-nc': {
            'label': 'Creative Commons Attribution-NonCommercial 4.0 International License',
            'value': 'CC-BY-NC'
        }
    }

    try:
        constraint_key = re.search(r"licenses\/([^\/]+)\/", value).group(1)
        return constraints[constraint_key]
    except:
        return {
            'label': 'Other constraints',
            'value': 'OTHER',
            'other': True
        }

def online_methods(value):
    return [{
        'title': v.get('name'),
        'url': v.get('uri') if not v.get('uri') in ["XXX", "XXXx"] else None
    } for v in value] if value != None else None

def data_sources(value):
    return [{
        'transferOptions': {
            'description': v.get('description'),
            'name': v.get('name'),
            'protocol': v.get('protocol'),
            'linkage': v.get('url')
        },
        'distributor': {
            'is_dissolved': 'false',
            'name': 'TERN Ecosystem Processes - UQ Long Pocket',
            'full_address_line': 'Building 1019, 80 Meiers Rd, Indooroopilly, QLD, Australia, 4068',
            'postcode': '4068',
            'address_region': 'QLD',
            'address_locality': 'Indooroopilly',
            'date_modified': '2021-05-14T05:18:27.281Z',
            'street_address': 'Building 1019, 80 Meiers Rd',
            'date_created': '2021-05-14T03:33:48.260Z',
            'uri': 'https://w3id.org/tern/resources/8f2acf9f-3cf2-48c7-b911-ed1b1113932e',
            'display_name': 'TERN Ecosystem Processes - UQ Long Pocket',
            'site_uri': 'https://w3id.org/tern/resources/fa56a1ed-ec38-4294-90ae-ab203a25d5ad',
            'country': 'Australia'
        },
        'uri': str(uuid.uuid4()),
        'isUserDefined': True
    } for v in value] if value != None else None

def boxes(value):
    return [{
        'northBoundLatitude': v.get('northBoundLatitude'),
        'northBoundLatitude': v.get('southBoundLatitude'),
        'northBoundLatitude': v.get('eastBoundLongitude'),
        'northBoundLatitude': v.get('westBoundLongitude'),
        'uri': str(uuid.uuid4())
    } for v in value] if value != None else None

def full_address_line(value):
    return f"{value['deliveryPoint']} {value['city']} {value['administrativeArea']} {value['postalCode']} {value['country']}" if value != None else None

def name(value):
    return f"{value.get('givenName')} {value.get('familyName')}" if (value != None and value.get('givenName') and value.get('familyName')) else None

def party_type(value):
    return 'organisation' if (value.get('givenName') in ['a_not_applicable', '', None]) else 'person'

def cited_responsible_party(value):
    return [{
        'role': role(v.get('role')),
        'organisation': {
            'address_locality': v.get('address', {}).get('city'),
            'street_address': v.get('address', {}).get('deliveryPoint'),
            'postcode': v.get('address', {}).get('postalCode'),
            'country': v.get('address', {}).get('country'),
            'address_region': v.get('address', {}).get('administrativeArea'),
            'full_address_line': full_address_line(v.get('address')),
            'name': v.get('organisationName'),
            'uri': str(uuid.uuid4()),
            'isUserDefined': v.get('isUserAdded'),
            'email': None, #TODO
            'userAddedCategory': None, #TODO
            'date_modified': None, #TODO
            'display_name': v.get('organisationName'),
            'is_dissolved': None, #TODO
            'date_created': None #TODO
        },
        'contact': {
            'canonical_name': v.get('individualName'),
            'orcid': v.get('orcid'),
            'email': v.get('electronicMailAddress'),
            'isUserDefined': v.get('isUserAdded'),
            'surname': v.get('familyName'),
            'given_name': v.get('givenName'),
            'name': name(v),
            'uri': str(uuid.uuid4()),
            'userAddedCategory': None, #TODO
            'date_modified': None, #TODO
            'date_created': None #TODO
        },
        'uri': str(uuid.uuid4()),
        'isUserDefined': v.get('isUserAdded'),
        'partyType': party_type(v)
    } for v in value] if value != None else None

def tern_topic_categories(value):
    return [{
        'value': v,
        'label': v.capitalize(),
        'uri': str(uuid.uuid4())
    } for v in value] if value != None else None

functions = {
    'todo': lambda value: None,
    'capitalize': lambda value: value.capitalize(),
    'parseNum': parse_num,
    'exists': lambda value: value != None and len(value) != 0,
    'fullAddressLine': full_address_line,
    'name': name,
    'partyType': party_type,
    'otherConstraints': other_constraints,
    'true': lambda value: True,
    'join': lambda value: ("\n".join(list(filter(None, value))) if isinstance(value, list) else value) if value != None else None,
    'protocol': protocol,
    'role': role,
    'uuid': lambda value: value or str(uuid.uuid4()),
    'parameter': parameter,
    'platform': platform,
    'instrument': instrument,
    'keywordsTheme': keywordsTheme,
    'keywordsThemeAnzsrc': keywordsThemeAnzsrc,
    'keywordsHorizontal': keywordsHorizontal,
    'keywordsTemporal': keywordsTemporal,
    'distributor': distributor,
    'keywordsAdditional': lambda value: (value.get('keywordsThemeExtra', {}).get('keywords', []) + value.get('keywordsTaxonExtra', {}).get('keywords', [])) if value != None else None,
    'topicCategories': lambda value: [{'label': value, 'value': value}],
    'status': lambda value: value if value != 'complete' else 'completed',
    'imas_keywordsTheme': lambda value: [{'label': f"https://gcmdservices.gsfc.nasa.gov/kms/concept/{v}", 'uri': f"https://gcmdservices.gsfc.nasa.gov/kms/concept/{v}"} for v in value],
    'verticalCRS': vertical_crs,
    'dataParametersName': lambda value: value['longName'] if value['longName'] != value['name'] else None,
    'creativeCommons': creative_commons,
    'list': lambda value: [value],
    'filter': lambda value, args: value if value not in args.get('matches') else None,
    'onlineMethods': online_methods,
    'dataSources': data_sources,
    'boxes': boxes,
    'citedResponsibleParty': cited_responsible_party,
    'ternTopicCategories': tern_topic_categories
}

def get_data_at_path(data, path):
    data = copy.deepcopy(data)
    path = copy.deepcopy(path)

    if data == None:
        return data

    if len(path) > 0:
        key = path.pop(0)
        if type(key) is list:
            return [get_data_at_path(d, key) for d in data]
        else:
            if not (type(data) is list and key >= len(data)):
                return get_data_at_path(data.get(key), path)
            else:
                return None
    else:
        return data

def set_data_at_path(data, path, value):
    data = copy.deepcopy(data)
    path = copy.deepcopy(path)

    if value == None:
        return data

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

def apply(value, depth, fn, args):
    value = copy.deepcopy(value)

    if depth == 0:
        return fn(value, args) if args else fn(value)
    elif value != None:
        return [apply(v, depth - 1, fn, args) for v in value]
    else:
        return value

def do_migration(input, output, migration):
    src = migration['src']
    dst = migration['dst']
    value = get_data_at_path(input, src)

    fn = migration.get('fn')
    fn_name = None
    fn_args = None

    if type(fn) is str:
        fn_name = fn
    elif type(fn) is dict:
        fn_name = fn.get('name')
        fn_args = fn.get('args')
    
    fn = functions.get(fn_name)

    if not clear_empty_keys(value): # if the untransformed source is empty
        value = get_data_at_path(input, dst) # try getting from the destination.
        if not clear_empty_keys(value) and fn: # assuming the destination is empty, and we have a function,
            value = apply(value, depth(dst), fn, fn_args) # try applying that function.
    elif fn: # if the untransformed source isn't empty, and we have a function
        value = apply(value, depth(dst), fn, fn_args) # try applying that function.
        if not clear_empty_keys(value): # if applying the function resulted in empty data
            value = get_data_at_path(input, dst) # just grab whatever is at the destination.

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

def migrate_data(input_data, template, migrations):
    output_data = copy.deepcopy(template)
    for migration in migrations:
        output_data = do_migration(input_data, output_data, migration)
    output_data = clear_empty_keys(output_data)
    return output_data