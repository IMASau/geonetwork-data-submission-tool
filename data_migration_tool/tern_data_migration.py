import json
import copy

# My sincerest apologies, because this script is bad
#
# TODOs under headings indicate paths in original data that either don't map to
# anywhere in new data, or that need to perform some sort of lookup before the
# migration (and as such neither has been done)

def get_data_at_path(data, path):
    path = copy.deepcopy(path)
    try:
        while len(path) > 0:
            key = path.pop(0)
            if type(data) is not dict:
                raise TypeError
            data = data[key]
        return data
    except:
        return None

def set_data_at_path(data, path, value):
    path = copy.deepcopy(path)
    key = path.pop(0)
    if len(path) == 0:
        data[key] = value
    else:
        if key not in data.keys():
            data[key] = {}
        data[key] = set_data_at_path(data[key], path, value)
    return data

def copy_data_at_path(input, output, path, output_path=None):
    if output_path is None:
        output_path = copy.deepcopy(path)
    return set_data_at_path(output, output_path, get_data_at_path(input, path))

def copy_array_at_path(input, output, path, item_paths, output_path=None, output_item_paths=None):
    if output_path is None:
        output_path = copy.deepcopy(path)
    if output_item_paths is None:
        output_item_paths = copy.deepcopy(item_paths)
    
    input_items = get_data_at_path(input, path)
    output_items = []
    for input_item in input_items:
        output_item = {}
        for i, item_path in enumerate(item_paths):
            output_item = copy_data_at_path(input_item, output_item, item_path, output_item_paths[i])
        output_items.append(output_item)
    output = set_data_at_path(output, output_path, output_items)
    
    return output

def clear_empty_keys(data):
    data = copy.deepcopy(data)
    if type(data) is dict:
        data = dict((k, clear_empty_keys(v)) for k, v in data.items())
        data = dict((k, v) for k, v in data.items() if not (v == None or v == {} or v == [] or v == ""))

        # for key in keys:
        #     new_data[key] = clear_empty_keys(data[key])
        #     if (new_data[key] is None):
        #         del new_data[key]
        #data = new_data
    if type(data) is list:
        data = [clear_empty_keys(v) for v in data]
        data = [v for v in data if not (v == None or v == {} or v == [] or v == "")]
    return data



filepath = input('Please input the filepath to the data you wish to migrate: ')

input = json.loads(open(filepath, 'r').read())

output = {}

# distributionInfo
# TODO

# fileIdentifier
output = copy_data_at_path(input, output, ['fileIdentifier'])

# resourceLineage

# resourceLineage > lineage
output = copy_data_at_path(input, output, ['resourceLineage', 'lineage'], output_path=['resourceLineage', 'statement'])

# resourceLineage > processStep
output = copy_array_at_path(input, output, ['resourceLineage', 'processStep'], [['name'], ['uri']], output_path=['resourceLineage', 'onlineMethods'], output_item_paths=[['title'], ['uri']])

# resourceLineage > processStep > description
# TODO

# dataSources
output = copy_array_at_path(input, output, ['dataSources'], [['description'], ['name'], ['protocol'], ['url']], output_item_paths=[['description'], ['name'], ['protocol'], ['linkage']])

# attachments
output = copy_array_at_path(input, output, ['attachments'], [['file'], ['name'], ['delete_url']])

# dataQualityInfo

# dataQualityInfo > methods
output = copy_data_at_path(input, output, ['dataQualityInfo', 'methods'], output_path=['dataQualityInfo', 'methodSummary'])

# dataQualityInfo > results
output = copy_data_at_path(input, output, ['dataQualityInfo', 'results'])

# supportingResources
# TODO

# noteForDataManager
# TODO

# identificationInfo

# identificationInfo > keywordsTaxonExtra

# identificationInfo > keywordsTaxonExtra > keywords
# TODO

# identificationInfo > keywordsTheme

# identificationInfo > keywordsTheme > keywords
output = copy_array_at_path(input, output, ['identificationInfo', 'keywordsTheme', 'keywords'], [[]], output_item_paths=[['uri']])

# identificationInfo > dataParameters
output = copy_array_at_path(input, output, ['identificationInfo', 'dataParameters'], [['serialNumber'], ['instrument_vocabularyVersion'], ['instrument_vocabularyTermURL'], ['instrument_termDefinition'], ['instrument_term']], output_path=['identificationInfo', 'keywordsInstrument', 'keywords'], output_item_paths=[['serial'], ['source'], ['uri'], ['description'], ['label']])
output = copy_array_at_path(input, output, ['identificationInfo', 'dataParameters'], [['platform_term'], ['platform_vocabularyVersion'], ['platform_termDefinition'], ['platform_vocabularyTermURL']], output_path=['identificationInfo', 'keywordsPlatform', 'keywords'], output_item_paths=[['label'], ['source'], ['description'], ['uri']])
output = copy_array_at_path(input, output, ['identificationInfo', 'dataParameters'], [['longName_vocabularyTermURL'], ['unit_termDefinition'], ['longName_term'], ['unit_vocabularyVersion'], ['unit_vocabularyTermURL'], ['longName_vocabularyVersion'], ['longName_termDefinition'], ['unit_term']], output_path=['identificationInfo', 'keywordsParameters', 'keywords'], output_item_paths=[['uri'], ['unit', 'description'], ['label'], ['unit', 'source'], ['unit', 'uri'], ['source'], ['description'], ['unit', 'label']])

# identificationInfo > dataParameters > name
# TODO

# identificationInfo > horizontalResolution
horizontal_resolution = get_data_at_path(input, ['identificationInfo', 'horizontalResolution'])[0] if len(get_data_at_path(input, ['identificationInfo', 'horizontalResolution'])) > 0 else None

# identificationInfo > horizontalResolution > prefLabel
output = copy_data_at_path(horizontal_resolution, output, ['prefLabel'], output_path=['identificationInfo', 'keywordsHorizontal', 'label'])

# identificationInfo > horizontalResolution > uri
output = copy_data_at_path(horizontal_resolution, output, ['uri'], output_path=['identificationInfo', 'keywordsHorizontal', 'uri'])

# identificationInfo > dateRevision
# TODO

# identificationInfo > endPosition
output = copy_data_at_path(input, output, ['identificationInfo', 'endPosition'])

# identificationInfo > verticalElement

# identificationInfo > verticalElement > hasVerticalExtent
# TODO

# identificationInfo > verticalElement > minimumValue
output = set_data_at_path(output, ['identificationInfo', 'minimumValue'], str(get_data_at_path(input, ['identificationInfo', 'minimumValue'])))

# identificationInfo > verticalElement > maximumValue
output = set_data_at_path(output, ['identificationInfo', 'maximumValue'], str(get_data_at_path(input, ['identificationInfo', 'maximumValue'])))

# identificationInfo > verticalElement > method
# TODO

# identificationInfo > verticalElement > elevation
# TODO

# identificationInfo > purpose
output = copy_data_at_path(input, output, ['identificationInfo', 'purpose'])

# identificationInfo > abstract
output = copy_data_at_path(input, output, ['identificationInfo', 'abstract'])

# identificationInfo > dateCreation
output = copy_data_at_path(input, output, ['identificationInfo', 'dateCreation'])

# identificationInfo > geographicElement

# identificationInfo > hasGeographicCoverage
# TODO

# identificationInfo > geographicElement > siteDescription
output = copy_data_at_path(input, output, ['identificationInfo', 'geographicElement', 'siteDescription'])

# identificationInfo > geographicElement > boxes

# identificationInfo > geographicElement > boxes > eastBoundLongitude
output = copy_data_at_path(input, output, ['identificationInfo', 'geographicElement', 'boxes', 'eastBoundLongitude'])

# identificationInfo > geographicElement > boxes > eastBoundLongitude
output = copy_data_at_path(input, output, ['identificationInfo', 'geographicElement', 'boxes', 'northBoundLatitude'])

# identificationInfo > geographicElement > boxes > eastBoundLongitude
output = copy_data_at_path(input, output, ['identificationInfo', 'geographicElement', 'boxes', 'southBoundLatitude'])

# identificationInfo > geographicElement > boxes > eastBoundLongitude
output = copy_data_at_path(input, output, ['identificationInfo', 'geographicElement', 'boxes', 'westBoundLongitude'])

# identificationInfo > doi
# TODO

# identificationInfo > title
output = copy_data_at_path(input, output, ['identificationInfo', 'title'])

# identificationInfo > citedResponsibleParty
output = copy_array_at_path(input, output, ['identificationInfo', 'citedResponsibleParty'], [['role'],  ['address'],  ['address', 'deliveryPoint'], ['address', 'city'],  ['address', 'administrativeArea'],  ['address', 'postalCode'],  ['address', 'country'],  ['individualName'],  ['phone'],  ['isUserAdded'],  ['facsimile'],  ['orcid'],  ['familyName'],  ['organisationIdentifier'],  ['electronicMailAddress'],  ['givenName'],  ['uri']], output_item_paths=[['role', 'Identifier'], ['organisation'], ['organisation', 'street_address'], ['organisation', 'city'], ['organisation', 'address_region'], ['organisation', 'postcode'], ['organisation', 'country'], ['contact', 'canonical_name'], ['organisation', 'phone'], ['isUserDefined'], ['organisation', 'fax'], ['contact', 'orcid'], ['contact', 'surname'], ['organisation', 'uri'], ['contact', 'email'], ['contact', 'given_name'], ['contact', 'uri']])

# identificationInfo > citedResponsibleParty > address > deliveryPoint2
# TODO

# identificationInfo > citedResponsibleParty > positionName
# TODO

# identificationInfo > datePublication
output = copy_data_at_path(input, output, ['identificationInfo', 'datePublication'])

# identificationInfo > status
output = copy_data_at_path(input, output, ['identificationInfo', 'status'])

# identificationInfo > topicCategory
output = copy_data_at_path(input, output, ['identificationInfo', 'topicCategory'])

# identificationInfo > supplementalInformation
output = copy_data_at_path(input, output, ['identificationInfo', 'supplementalInformation'], output_path=['identificationInfo', 'supplemental'])

# identificationInfo > keywordsAdditional

# identificationInfo > keywordsAdditional > keywords
output = copy_data_at_path(input, output, ['identificationInfo', 'keywordsAdditional', 'keywords'], output_path=['identificationInfo', 'keywordsThemeExtra', 'keywords'])

# identificationInfo > keywordsThemeAnzsrc

# identificationInfo > keywordsThemeAnzsrc > keywords
output = copy_array_at_path(input, output, ['identificationInfo', 'keywordsThemeAnzsrc', 'keywords'], [[]], output_item_paths=[['uri']])

# identificationInfo > geographicElementSecondary
# TODO

# identificationInfo > maintenanceAndUpdateFrequency
output = copy_data_at_path(input, output, ['identificationInfo', 'maintenanceAndUpdateFrequency'])

# identificationInfo > pointOfContact
output = copy_array_at_path(input, output, ['identificationInfo', 'pointOfContact'], [['role'],  ['address'],  ['address', 'deliveryPoint'], ['address', 'city'],  ['address', 'administrativeArea'],  ['address', 'postalCode'],  ['address', 'country'],  ['individualName'],  ['phone'],  ['isUserAdded'],  ['facsimile'],  ['orcid'],  ['familyName'],  ['organisationIdentifier'],  ['electronicMailAddress'],  ['givenName'],  ['uri']], output_item_paths=[['role', 'Identifier'], ['organisation'], ['organisation', 'street_address'], ['organisation', 'city'], ['organisation', 'address_region'], ['organisation', 'postcode'], ['organisation', 'country'], ['contact', 'canonical_name'], ['organisation', 'phone'], ['isUserDefined'], ['organisation', 'fax'], ['contact', 'orcid'], ['contact', 'surname'], ['organisation', 'uri'], ['contact', 'email'], ['contact', 'given_name'], ['contact', 'uri']])

# identificationInfo > pointOfContact > address > deliveryPoint2
# TODO

# identificationInfo > pointOfContact > positionName
# TODO

# identificationInfo > useLimitations
useLimitations = ["\n            The CC-By license allows others to copy, distribute, display, and create derivative works provided that they credit the original source and any other nominated parties. Details are provided at https://creativecommons.org/licenses/by/4.0/legalcode\n          "] + get_data_at_path(input, ['identificationInfo', 'useLimitations'])
output = set_data_at_path(output, ['identificationInfo', 'useLimitation'], '\n'.join(useLimitations))

# identificationInfo > credit
output = set_data_at_path(output, ['identificationInfo', 'credit'], '\n'.join(get_data_at_path(input, ['identificationInfo', 'credit'])))

# identificationInfo > samplingFrequency

# identificationInfo > samplingFrequency > prefLabel
output = copy_data_at_path(input, output, ['identificationInfo', 'samplingFrequency', 'prefLabel'], output_path=['identificationInfo', 'keywordsTemporal', 'keywords', 'label'])

# identificationInfo > samplingFrequency > uri
output = copy_data_at_path(input, output, ['identificationInfo', 'samplingFrequency', 'uri'], output_path=['identificationInfo', 'keywordsTemporal', 'keywords', 'uri'])

# identificationInfo > beginPosition
output = copy_data_at_path(input, output, ['identificationInfo', 'beginPosition'])

# who-authorRequired
# TODO

# doiRequested
# TODO

# agreedToTerms
# TODO

output = clear_empty_keys(output)
# print(output)

open('output.json', 'wt').write(json.dumps(output))