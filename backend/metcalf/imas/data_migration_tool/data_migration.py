import copy
import uuid
import re

import functools

def coalesce(*arg):
  return functools.reduce(lambda x, y: x if x is not None else y, arg)

def parse_num(value):
    try:
        v = float(value)
        return v
    except:
        return value

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
        'Description': 'Party who uses the resource'},
        {'UUID': '6511df52-a5ff-42da-8788-34dcad38ccc8',
        'Identifier': 'pointOfContact',
        'Description': 'Party who can be contacted for acquiring knowledge about or acquisition of the resource'}
    ]
    try:
        return next(v for v in roles if v['Identifier'] == value) if isinstance(value, str) else value
    except:
        return {'Identifier': value}

def tern_parameters_units(value):
    if value == None:
        return None
    else:
        parameters_units = []

        for parameter_unit in value:
            parameter_label =  coalesce(parameter_unit.get('longName_term'), '')
            parameter_description =  parameter_unit.get('longName_termDefinition')
            parameter_source =  parameter_unit.get('longName_vocabularyVersion')
            parameter_uri =  parameter_unit.get('longName_vocabularyTermURL') if parameter_unit.get('longName_vocabularyTermURL') != 'http://linkeddata.tern.org.au/XXX' else str(uuid.uuid4())
            unit_label =  coalesce(parameter_unit.get('unit_term'), '')
            unit_source =  parameter_unit.get('unit_vocabularyVersion')
            unit_uri =  parameter_unit.get('unit_vocabularyTermURL') if parameter_unit.get('longName_vocabularyTermURL') != 'http://linkeddata.tern.org.au/XXX' else str(uuid.uuid4())

            parameter = {
                'label': parameter_label,
                'description': parameter_description,
                'uri': parameter_uri,
                'source': parameter_source
                } if len(parameter_label) > 0 else None
            
            unit = {
                'label': unit_label,
                'uri': unit_uri,
                'source': unit_source
                } if len(unit_label) > 0 else {
                'label': 'Unitless',
                'uri': 'http://qudt.org/vocab/unit/UNITLESS',
                'source': None
                } if parameter else None

            if parameter or unit:
                parameters_units.append({
                    'parameter': parameter,
                    'unit': unit,
                    'uri' : str(uuid.uuid4())
                })
        
        return parameters_units

def instrument(value):
    return [{
        'serial': v.get('serialNumber'),
        'label': v.get('instrument_term'),
        'description': v.get('instrument_termDefinition'),
        'uri': v.get('instrument_vocabularyTermURL') if v.get('instrument_vocabularyTermURL') != 'http://linkeddata.tern.org.au/XXX' else str(uuid.uuid4()),
        'source': v.get('instrument_vocabularyVersion')
    } for v in value] if value != None else None

def tern_platforms(value):
    if value == None:
        return None
    else:
        platforms = []

        for platform in value:
            label =  coalesce(platform.get('platform_term'), '')
            description =  platform.get('platform_termDefinition')
            source =  platform.get('platform_vocabularyVersion')
            uri =  platform.get('platform_vocabularyTermURL') if platform.get('platform_vocabularyTermURL') != 'http://linkeddata.tern.org.au/XXX' else str(uuid.uuid4())

            if len(label) > 0:
                platforms.append({
                    'userAddedCategory': 'platform',
                    'label': label,
                    'description': description,
                    'uri': uri,
                    'source': source
                })

        return platforms

def keywordsHorizontal(value):
    return {
        # 'breadcrumb': TODO,
        'label': value[0]['prefLabel'],
        'uri': value[0]['uri'],
        # 'broader_concept': TODO
    } if (value != None and len(value) > 0) else None

def keywordsTemporal(value):
    return keywordsHorizontal(value)

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
    if not isinstance(value, str):
        return value

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

def tern_steps(value):
    if value == None:
        return None
    else:
        steps = []
        for step in value:
            name = step.get('name', '')
            description = step.get('description', '')
            if len(name) > 0 and len(description) > 0:
                steps.append(name + ':\n' + description)
            else:
                steps.append(name + description)
        return steps

def data_sources(value):
    return [{
        'transferOptions': {
            'description': coalesce(v.get('transferOptions', {}).get('description'), v.get('name')),
            'name': coalesce(v.get('transferOptions', {}).get('name'), v.get('description')),
            'protocol': coalesce(v.get('transferOptions', {}).get('protocol'), protocol(v.get('protocol'))),
            'linkage': coalesce(v.get('transferOptions', {}).get('linkage'), v.get('url'))
        },
        'distributor': {
            'email': 'esupport@tern.org.au',
            'is_dissolved': 'false',
            'telephone': '+61 7 3365 9097',
            'name': 'Terrestrial Ecosystem Research Network',
            'full_address_line': 'Building 1019, 80 Meiers Rd, Indooroopilly, QLD, Australia, 4068',
            'postcode': '4068',
            'address_region': 'QLD',
            'address_locality': 'Indooroopilly',
            'date_modified': '2021-02-12T07:20:44.102Z',
            'street_address': 'Building 1019, 80 Meiers Rd',
            'date_created': '2020-01-29T10:48:55.728149',
            'uri': 'https://w3id.org/tern/resources/a083902d-d821-41be-b663-1d7cb33eea66',
	        'display_name': 'Terrestrial Ecosystem Research Network - UQ Long Pocket',
	        'site_uri': 'https://w3id.org/tern/resources/fa56a1ed-ec38-4294-90ae-ab203a25d5ad',
	        'country': 'Australia'
        },
        'uri': coalesce(v.get('uri'), str(uuid.uuid4())),
        'isUserDefined': coalesce(v.get('transferOptions', {}).get('isUserDefined'), True)
    } for v in value] if value != None else None

def boxes(value):
    return [{
        'northBoundLatitude': v.get('northBoundLatitude'),
        'southBoundLatitude': v.get('southBoundLatitude'),
        'eastBoundLongitude': v.get('eastBoundLongitude'),
        'westBoundLongitude': v.get('westBoundLongitude'),
        'uri': coalesce(v.get('uri'), str(uuid.uuid4())),
        'isUserDefined': True
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
            'address_locality': coalesce(v.get('organisation', {}).get('address_locality'), v.get('address', {}).get('city')),
            'street_address': coalesce(v.get('organisation', {}).get('street_address'), v.get('address', {}).get('deliveryPoint')),
            'postcode': coalesce(v.get('organisation', {}).get('postcode'), v.get('address', {}).get('postalCode')),
            'country': coalesce(v.get('organisation', {}).get('country'), v.get('address', {}).get('country')),
            'address_region': coalesce(v.get('organisation', {}).get('address_region'), v.get('address', {}).get('administrativeArea')),
            'full_address_line': coalesce(v.get('organisation', {}).get('full_address_line'), full_address_line(v.get('address'))),
            'name': coalesce(v.get('organisation', {}).get('name'), v.get('organisationName')),
            'uri': coalesce(v.get('organisation', {}).get('uri'), str(uuid.uuid4())),
            'isUserDefined': coalesce(v.get('organisation', {}).get('isUserDefined'), v.get('isUserAdded')),
            'email': coalesce(v.get('organisation', {}).get('email'), None), #TODO
            'userAddedCategory': coalesce(v.get('organisation', {}).get('userAddedCategory'), None), #TODO
            'date_modified': coalesce(v.get('organisation', {}).get('date_modified'), None), #TODO
            'display_name': coalesce(v.get('organisation', {}).get('display_name'), v.get('organisationName')),
            'is_dissolved': coalesce(v.get('organisation', {}).get('is_dissolved'), None), #TODO
            'date_created': coalesce(v.get('organisation', {}).get('date_created'), None) #TODO
        },
        'contact': {
            'canonical_name': coalesce(v.get('contact', {}).get('canonical_name'), v.get('individualName')),
            'orcid': coalesce(v.get('contact', {}).get('orcid'), v.get('orcid')),
            'email': coalesce(v.get('contact', {}).get('email'), v.get('electronicMailAddress')),
            'isUserDefined': coalesce(v.get('contact', {}).get('isUserDefined'), v.get('isUserAdded')),
            'surname': coalesce(v.get('contact', {}).get('surname'), v.get('familyName')),
            'given_name': coalesce(v.get('contact', {}).get('given_name'), v.get('givenName')),
            'name': coalesce(v.get('contact', {}).get('name'), name(v)),
            'uri': coalesce(v.get('contact', {}).get('uri'), str(uuid.uuid4())),
            'userAddedCategory': coalesce(v.get('contact', {}).get('userAddedCategory'), None), #TODO
            'date_modified': coalesce(v.get('contact', {}).get('date_modified'), None), #TODO
            'date_created': coalesce(v.get('contact', {}).get('date_created'), None) #TODO
        },
        'uri': coalesce(v.get('uri'), str(uuid.uuid4())),
        'isUserDefined': coalesce(v.get('isUserDefined'), v.get('isUserAdded')),
        'partyType': coalesce(v.get('partyType'), party_type(v))
    } for v in value] if value != None else None

def tern_topic_categories(value):
    return [{
        'value': v,
        'label': v.capitalize(),
        'uri': str(uuid.uuid4())
    } for v in value] if value != None else None

def additional_publications(value):
    return [{
        'title': v.get('name'),
        'url': v.get('url'),
        'uri': str(uuid.uuid4()),
        'isUserDefined': True,
        'userAddedCategory': None # TODO
    } for v in value] if value != None else None

def point_of_contact(value):
    return [{
        'role': role('pointOfContact'),
        'organisation': {
            'address_locality': coalesce(v.get('organisation', {}).get('address_locality'), v.get('address', {}).get('city')),
            'street_address': coalesce(v.get('organisation', {}).get('street_address'), v.get('address', {}).get('deliveryPoint')),
            'postcode': coalesce(v.get('organisation', {}).get('postcode'), v.get('address', {}).get('postalCode')),
            'country': coalesce(v.get('organisation', {}).get('country'), v.get('address', {}).get('country')),
            'address_region': coalesce(v.get('organisation', {}).get('address_region'), v.get('address', {}).get('administrativeArea')),
            'full_address_line': coalesce(v.get('organisation', {}).get('full_address_line'), full_address_line(v.get('address'))),
            'name': coalesce(v.get('organisation', {}).get('name'), v.get('organisationName')),
            'uri': coalesce(v.get('organisation', {}).get('uri'), str(uuid.uuid4())),
            'isUserDefined': coalesce(v.get('organisation', {}).get('isUserDefined'), v.get('isUserAdded')),
            'email': coalesce(v.get('organisation', {}).get('email'), None), #TODO
            'userAddedCategory': coalesce(v.get('organisation', {}).get('userAddedCategory'), None), #TODO
            'date_modified': coalesce(v.get('organisation', {}).get('date_modified'), None), #TODO
            'display_name': coalesce(v.get('organisation', {}).get('display_name'), v.get('organisationName')),
            'is_dissolved': coalesce(v.get('organisation', {}).get('is_dissolved'), None), #TODO
            'date_created': coalesce(v.get('organisation', {}).get('date_created'), None) #TODO
        },
        'contact': {
            'canonical_name': coalesce(v.get('contact', {}).get('canonical_name'), v.get('individualName')),
            'orcid': coalesce(v.get('contact', {}).get('orcid'), v.get('orcid')),
            'email': coalesce(v.get('contact', {}).get('email'), v.get('electronicMailAddress')),
            'isUserDefined': coalesce(v.get('contact', {}).get('isUserDefined'), v.get('isUserAdded')),
            'surname': coalesce(v.get('contact', {}).get('surname'), v.get('familyName')),
            'given_name': coalesce(v.get('contact', {}).get('given_name'), v.get('givenName')),
            'name': coalesce(v.get('contact', {}).get('name'), name(v)),
            'uri': coalesce(v.get('contact', {}).get('uri'), str(uuid.uuid4())),
            'userAddedCategory': coalesce(v.get('contact', {}).get('userAddedCategory'), None), #TODO
            'date_modified': coalesce(v.get('contact', {}).get('date_modified'), None), #TODO
            'date_created': coalesce(v.get('contact', {}).get('date_created'), None) #TODO
        },
        'uri': coalesce(v.get('uri'), str(uuid.uuid4())),
        'isUserDefined': coalesce(v.get('isUserDefined'), v.get('isUserAdded')),
        'partyType': coalesce(v.get('partyType'), party_type(v))
    } for v in value] if value != None else None

def keywords_additional(value):
    return value.get('keywordsAdditional', {}).get('keywords') or ((value.get('keywordsThemeExtra', {}).get('keywords', []) + value.get('keywordsTaxonExtra', {}).get('keywords', [])) if value != None else None)

def imas_geographic_element(value):
    return [{
        'northBoundLatitude': v.get('northBoundLatitude'),
        'southBoundLatitude': v.get('southBoundLatitude'),
        'eastBoundLongitude': v.get('eastBoundLongitude'),
        'westBoundLongitude': v.get('westBoundLongitude'),
        'isUserDefined': True,
        'uri': coalesce(v.get('uri'), str(uuid.uuid4()))
    } for v in value] if value != None else None

def imas_cited_responsible_party(value):
    return [{
        'role': v.get('role'),
        'contact': {
            'name': coalesce(v.get('contact', {}).get('name'), v.get('individualName')),
            'orcid2': coalesce(v.get('contact', {}).get('orcid2'), v.get('orcid')),
            'deliveryPoint': coalesce(v.get('contact', {}).get('deliveryPoint'), v.get('address', {}).get('deliveryPoint')),
            'deliveryPoint2': coalesce(v.get('contact', {}).get('deliveryPoint2'), v.get('address', {}).get('deliveryPoint2')),
            'city': coalesce(v.get('contact', {}).get('city'), v.get('address', {}).get('city')),
            'administrativeArea': coalesce(v.get('contact', {}).get('administrativeArea'), v.get('address', {}).get('administrativeArea')),
            'postalCode': coalesce(v.get('contact', {}).get('postalCode'), v.get('address', {}).get('postalCode')),
            'country': coalesce(v.get('contact', {}).get('country'), v.get('address', {}).get('country')),
            'email': coalesce(v.get('contact', {}).get('email'), v.get('electronicMailAddress')),
            'phone': coalesce(v.get('contact', {}).get('phone'), v.get('phone')),
            'facsimile': coalesce(v.get('contact', {}).get('facsimile'), v.get('facsimile'))
        },
        'organisation' : {
            'name': coalesce(v.get('organisation', {}).get('name'), v.get('organisationName')),
        },
        'uri': coalesce(v.get('uri'), str(uuid.uuid4())),
        'isUserDefined': True
    } for v in value] if value != None else None

def imas_point_of_contact(value):
    return imas_cited_responsible_party(value)

def imas_data_parameters(value):
    return [{
        'longName_term': {
            'Name': coalesce(v.get('longName_term', {}).get('Name'), v.get('name')),
            'URI': coalesce(v.get('longName_term', {}).get('URI'), str(uuid.uuid4())),
            'isUserDefined': True,
        },
        'unit_term': {
            'Name': coalesce(v.get('unit_term', {}).get('Name'), v.get('unit')),
            'URI': coalesce(v.get('unit_term', {}).get('URI'), str(uuid.uuid4())),
            'isUserDefined': True,
        },
        'name': (v.get('longName') if v.get('longName') != v.get('name') else None) if 'longName' in v.keys() else v.get('name'),
        'uri': coalesce(v.get('uri'), str(uuid.uuid4())),
        'isUserDefined': True,
        'parameterDescription': v.get('parameterDescription')
    } for v in value] if value != None else None

def aodn_data_parameters(value):
    if value == None:
        return None
    else:
        data_parameters = []

        for data_parameter in value:
            longName = coalesce(data_parameter.get('longName_term', {}).get('Name'), data_parameter.get('longName', {}).get('term'), '')
            unit = coalesce(data_parameter.get('unit_term', {}).get('Name'), data_parameter.get('unit', {}).get('term'), '')
            instrument = coalesce(data_parameter.get('instrument_term', {}).get('Name'), data_parameter.get('instrument', {}).get('term'), '')
            platform =coalesce(data_parameter.get('platform_term', {}).get('Name'), data_parameter.get('platform', {}).get('term'), '')

            longName_term = {
                'Name': longName,
                'URI': coalesce(data_parameter.get('longName_term', {}).get('URI'), data_parameter.get('longName', {}).get('vocabularyTermURL'), str(uuid.uuid4())),
                'isUserDefined': True,
            } if len(longName) > 0 else None
            unit_term = {
                'Name': unit,
                'URI': coalesce(data_parameter.get('unit_term', {}).get('URI'), data_parameter.get('unit', {}).get('vocabularyTermURL'), str(uuid.uuid4())),
                'isUserDefined': True,
            } if len(unit) > 0 else None
            instrument_term = {
                'Name': instrument,
                'URI': coalesce(data_parameter.get('instrument_term', {}).get('URI'), data_parameter.get('instrument', {}).get('vocabularyTermURL'), str(uuid.uuid4())),
                'isUserDefined': True,
            } if len(instrument) > 0 else None
            platform_term = {
                'Name': platform,
                'URI': coalesce(data_parameter.get('platform_term', {}).get('URI'), data_parameter.get('platform', {}).get('vocabularyTermURL'), str(uuid.uuid4())),
                'isUserDefined': True,
            } if len(platform) > 0 else None

            data_parameters.append({
                'longName_term': longName_term,
                'unit_term': unit_term,
                'instrument_term': instrument_term,
                'platform_term': platform_term,
                'name': data_parameter.get('name'),
                'uri': coalesce(data_parameter.get('uri'), str(uuid.uuid4())),
                'isUserDefined': True,
                'parameterDescription': data_parameter.get('parameterDescription')
            })
            
        return data_parameters

def aodn_attachments(value):
    if value == None:
        return None
    else:
        attachments = []
        
        for attachment in value:
            id = coalesce(attachment.get('id'))
            file = coalesce(attachment.get('file'))
            name = coalesce(attachment.get('name'))
            delete_url = coalesce(attachment.get('delete_url'))
            created = coalesce(attachment.get('created'))
            modified = coalesce(attachment.get('modified'))

            if id == None and delete_url != None:
                id = int(re.search(r"\/(\d+)\/$", delete_url).group(1))
            if file != None:
                file = re.search(r"\/media.+", file).group(0)
            if delete_url != None:
                delete_url = re.search(r"\/delete.+", delete_url).group(0)

            attachments.append({
                'id': id,
                'file': file,
                'name': name,
                'delete_url': delete_url,
                'created': created,
                'modified': modified
            })
        
        return attachments

def tern_attachments(value):
    if value == None:
        return None
    else:
        attachments = []
        
        for attachment in value:
            id = coalesce(attachment.get('id'))
            file = coalesce(attachment.get('file'))
            name = coalesce(attachment.get('name'))
            delete_url = coalesce(attachment.get('delete_url'))
            created = coalesce(attachment.get('created'))
            modified = coalesce(attachment.get('modified'))
            title = coalesce(attachment.get('title'), file)

            if id == None and delete_url != None:
                id = int(re.search(r"\/(\d+)\/$", delete_url).group(1))

            attachments.append({
                'id': id,
                'file': file,
                'name': name,
                'delete_url': delete_url,
                'created': created,
                'modified': modified,
                'title': title
            })
        
        return attachments

functions = {
    'todo': lambda value: None,
    'capitalize': lambda value: value.capitalize(),
    'parseNum': parse_num,
    'exists': lambda value: value != None and len(value) != 0,
    'fullAddressLine': full_address_line,
    'name': name,
    'partyType': party_type,
    'true': lambda value: True,
    'join': lambda value: ("\n".join(list(filter(None, value))) if isinstance(value, list) else value) if value != None else None,
    'protocol': protocol,
    'role': role,
    'uuid': lambda value: value or str(uuid.uuid4()),
    'tern_parametersUnits': tern_parameters_units,
    'instrument': instrument,
    'tern_platforms': tern_platforms,
    'tern_keywordsTheme': lambda value: [{'label': f"https://gcmdservices.gsfc.nasa.gov/kms/concept/{v}", 'uri': f"https://gcmdservices.gsfc.nasa.gov/kms/concept/{v}"} if isinstance(v, str) else v for v in value],
    'tern_keywordsThemeAnzsrc': lambda value: [{'label': v, 'uri': v} if isinstance(v, str) else v for v in value],
    'keywordsHorizontal': keywordsHorizontal,
    'keywordsTemporal': keywordsTemporal,
    'keywordsAdditional': keywords_additional,
    'topicCategories': lambda value: [{'label': value, 'value': value}] if isinstance(value, str) else value,
    'status': lambda value: value if value != 'complete' else 'completed',
    'imas_keywordsTheme': lambda value: [{'label': f"https://gcmdservices.gsfc.nasa.gov/kms/concept/{v}", 'uri': f"https://gcmdservices.gsfc.nasa.gov/kms/concept/{v}"} if isinstance(v, str) else v for v in value],
    'verticalCRS': vertical_crs,
    'creativeCommons': creative_commons,
    'list': lambda value: [value],
    'filter': lambda value, args: value if value not in args.get('matches') else None,
    'tern_steps': tern_steps,
    'dataSources': data_sources,
    'boxes': boxes,
    'citedResponsibleParty': cited_responsible_party,
    'ternTopicCategories': tern_topic_categories,
    'additionalPublications': additional_publications,
    'pointOfContact': point_of_contact,
    'imas_geographicElement': imas_geographic_element,
    'imas_pointOfContact': imas_point_of_contact,
    'imas_citedResponsibleParty': imas_cited_responsible_party,
    'imas_dataParameters': imas_data_parameters,
    'aodn_dataParameters': aodn_data_parameters,
    'aodn_attachments': aodn_attachments,
    'tern_attachments': tern_attachments
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

    value = get_data_at_path(input, src)

    if value == None:
        value = get_data_at_path(input, dst)
    elif fn:
        value = apply(value, 0, fn, fn_args)

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