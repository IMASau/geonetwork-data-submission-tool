import json
import os
import re
from functools import partial
from urllib.parse import urlsplit

from django.contrib.sites.models import Site

from metcalf.common.xmlutils4 import *


def select_keys(m, ks):
    return {k: m[k] for k in ks if k in m}


def update_vals(m, f):
    return {k: f(v) for (k, v) in m.items()}


def walk(inner, outer, form):
    ret = form.copy()  # shallow copy
    if ret.get('type') == 'array':
        ret['items'] = inner(ret['items'])
    elif ret.get('type') == 'object':
        ret['properties'] = update_vals(ret['properties'], inner)
    return outer(ret)


def postwalk(f, form):
    return walk(partial(postwalk, f), f, form)


def prewalk(f, form):
    return walk(partial(prewalk, f), lambda x: x, f(form))


def is_ref(schema):
    return '$ref' in schema


def get_ref_schema(defs, schema):
    assert '$ref' in schema
    assert schema['$ref'].startswith('#/$defs/')
    def_name = schema['$ref'].split('#/$defs/')[1]
    assert def_name
    return defs.get(def_name, {})


def insert_def(defs, schema):
    if is_ref(schema):
        return get_ref_schema(defs, schema)
    else:
        return schema


def inline_defs(schema):
    """
    replace any node group reference with the actual node_group
    """
    defs = schema.get('$defs')
    if defs:
        schema = prewalk(partial(insert_def, defs), schema)
        del schema['$defs']
    return schema


def full_xpaths_step(schema):
    """
    Push down xpaths and generate full_xpath annotations.

    :param schema:
    :return:
    """

    # Set full_xpath if needed and possible
    full_xpath = schema.get("full_xpath")
    xpath = schema.get("xpath")
    parent_xpath = schema.get("parent_xpath")
    if not full_xpath and xpath and parent_xpath:
        full_xpath = parent_xpath + "/" + xpath
        schema['full_xpath'] = full_xpath

    # Set parent_xpath if possible
    if full_xpath:
        schema_type = schema.get('type')
        if schema_type == 'object':
            for prop_name in schema['properties'].keys():
                schema['properties'][prop_name]['parent_xpath'] = full_xpath
        elif schema_type == 'array':
            schema['items']['parent_xpath'] = full_xpath

    # Clear parent_xpath if present (clean up)
    if parent_xpath:
        del schema['parent_xpath']

    return schema


def full_xpaths(schema):
    """
    Experimental analysis to resolve full_xpath based on schema tree and xpath annotations.
    """
    schema['full_xpath'] = schema['xpath']
    return prewalk(full_xpaths_step, schema)


def extract_field(schema):
    return select_keys(schema, ['label', 'type', 'rules', 'items', 'properties', 'required'])


def extract_fields(schema):
    return postwalk(extract_field, schema)


# TODO: Previously this had a side effect of validating the xml template.  Might need replacing.
def extract_schema(spec):
    return postwalk(extract_field, spec)


def remove_comment(schema):
    if isinstance(schema, dict):
        schema.pop(SpecialKeys.comment, None)
    return schema


def remove_comments(schema):
    return postwalk(remove_comment, schema)


def is_function_ref(spec):
    yes_or_no = isinstance(spec, dict) and SpecialKeys.function in spec
    return yes_or_no


def get_function_ref(x):
    transform = SPEC_FUNCTIONS[x[SpecialKeys.function]]
    return transform


# NOTE: Needs a review
def insert_functions(spec):
    """
    update string references to functions with the actual functions
    """
    if isinstance(spec, list):
        for sub_spec in spec:
            insert_functions(sub_spec)
    if isinstance(spec, dict):
        for key, sub_spec in spec.items():
            if isinstance(sub_spec, dict):
                if is_function_ref(sub_spec):
                    spec[key] = get_function_ref(sub_spec)
                insert_functions(sub_spec)
            elif isinstance(sub_spec, list):
                for i in sub_spec:
                    insert_functions(i)


# TODO: pass in app specific registrations?
def make_spec(**kwargs):
    assert 'mapper' in kwargs, "We couldn't load the mapper for this template. Please make sure the mapper exists"
    assert kwargs['mapper'] is not None, "No mapper exists for this template. Please specify one."
    spec = json.loads(kwargs['mapper'].file.read().decode('utf-8'))
    remove_comments(spec)
    spec = inline_defs(spec)
    insert_functions(spec)
    return spec


LINKAGE_UUID = re.compile(r'uuid=\w{8}-\w{4}-\w{4}-\w{4}-\w{12}')
KWARGS = None


def massage_version_number(s):
    """
    Version number captured is of format "version-1-1"
    Version required is "1.1"
    """
    re_version = r'version-([-\d]+)'
    if re.match(re_version, s):
        version_chunk = re.search(re_version, s).group(1)
        version_number = re.sub("-", ".", version_chunk)
        return version_number


def generate_attachment_url(**kwargs):
    # TODO: figure out how to get env here (was previously inline in the python spec)
    assert kwargs['data'] is not None, "data not provided"
    assert kwargs['uuid'] is not None, "uuid not provided"
    data = kwargs['data']
    uuid = kwargs['uuid']
    # data should be an absolute path to download the file
    if urlsplit(data).scheme:
        return data
    # generate absolute url using Site domain
    return 'https://{}{}'.format(
        Site.objects.get_current().domain,
        data
    )


def all_text(node):
    return ''.join(node.itertext()).strip()


def is_empty(node):
    return not all_text(node)


def prune_if_empty(data, parent, spec, nsmap, i, silent):
    """
    Catch-all processing to clean up specific elements that may have been
    left with no content.

    """
    # descriptiveKeywords without any content; ie not empty, but don't have a gmd:keyword
    for elem in parent.findall('.//mri:descriptiveKeywords', nsmap):
        if elem.find('./mri:MD_Keywords/mri:keyword', nsmap) is None:
            elem.getparent().remove(elem)
    for elem in parent.findall('.//mdb:parentMetadata', nsmap):
        # Use title as proxy for a value:
        if is_empty(elem.find('./cit:CI_Citation/cit:title', nsmap)):
            elem.getparent().remove(elem)
    # No descendent text() at all:
    for xpath in ['mri:descriptiveKeywords',
                  'mri:resourceConstraints']:
        for elem in parent.findall('.//' + xpath, nsmap):
            if is_empty(elem):
                elem.getparent().remove(elem)


def new_term_vocab_prune(data, parent, spec, nsmap, i, silent):
    """
    In case of a new term we need to prune some XML chunks from the template.

    NOTE: this is a late change so we're doing something safe but ugly.
    """
    if data['vocabularyTermURL']: return

    for xpath in ['mcp:vocabularyServiceURL', 'mcp:vocabularyPublisher', 'mcp:vocabularyTermPublisher']:
        for elem in parent.xpath(xpath, namespaces=nsmap):
            elem.getparent().remove(elem)


def create_linkage_uuid(x, y):
    return re.sub(LINKAGE_UUID, 'uuid=' + x, y)


def filename(x):
    return os.path.basename(x)


def today():
    return datetime.date.today().strftime('%Y-%m-%d')


def parse_keywords(x):
    return x.get('{http://www.w3.org/1999/xlink}href').split('/')[-1]


def science_keyword_from_uuid(x):
    return KWARGS['science_keyword'].objects.get(UUID=x).as_str()


def date_as_string(x):
    return datetime.datetime.strptime(x[: 10], '%Y-%m-%d').date().isoformat()


def has_geographic_coverage(x):
    return not x.get('hasGeographicCoverage', True)


def to_string(x):
    return str(x)


def has_vertical_extent(x):
    return not x.get('hasVerticalExtent', False)


def vertical_crs_identifier(x):
    return {'EPSG::5715': 'MSL depth', 'EPSG::5714': 'MSL height'}.get(x)


def person_uri(x):
    return x


def institution_uri(x):
    return x


def parse_vertical_elevation(x):
    for tok in x.text.split('|'):
        tok = tok.strip()
        if tok.startswith('Elevation in metres:'):
            tok = tok.replace('Elevation in metres:', '').strip().strip('[]')
            try:
                return str(float(tok))
            except:
                return ''
    return ''


def parse_vertical_method(x):
    for tok in x.text.split('|'):
        tok = tok.strip()
        if tok.startswith('Method:'):
            tok = tok.replace('Method:', '').strip().strip('[]')
            return tok
    return ''


def parse_individual_name(x):
    return x


def parse_individual_identifier(x):
    return x.attrib['{http://www.w3.org/1999/xlink}href']


def parse_individual_orcid(x):
    orcid_uri = x.attrib['{http://www.w3.org/1999/xlink}href']
    match = re.search(r"0000-000(1-[5-9]|2-[0-9]|3-[0-4])\d\d\d-\d\d\d[\dX]", orcid_uri)
    orcid = match.group(0)
    return orcid


def parse_codeListValue(x):
    return x.attrib['codeListValue']


def parse_organisation_identifier(x):
    if x.attrib['{http://www.w3.org/1999/xlink}role'] == 'uri':
        return x.attrib['{http://www.w3.org/1999/xlink}href']
    return None


def separate_organisation_identifier(x):
    if '||' in x:
        return x[:x.index('||')]
    return x


def science_keyword_name(**kwargs):
    assert kwargs['data'] != None, "data not provided"
    assert kwargs['models'] != None, "models not provided"
    data = kwargs['data']
    models = kwargs['models']
    keyword = models['backend']['sciencekeyword'].objects.get(pk=data)
    return keyword.DetailedVariable or keyword.VariableLevel3 or keyword.VariableLevel2 or keyword.VariableLevel1 or keyword.Term or keyword.Topic or keyword.Category


def science_keyword_uri(**kwargs):
    assert kwargs['data'] != None, "data not provided"
    assert kwargs['models'] != None, "models not provided"
    data = kwargs['data']
    models = kwargs['models']
    keyword = models['backend']['sciencekeyword'].objects.get(pk=data)
    return keyword.uri


def anzsrc_keyword_name(**kwargs):
    assert kwargs['data'] != None, "data not provided"
    assert kwargs['models'] != None, "models not provided"
    data = kwargs['data']
    models = kwargs['models']
    keyword = models['backend']['anzsrckeyword'].objects.get(pk=data)
    return keyword.DetailedVariable or keyword.VariableLevel3 or keyword.VariableLevel2 or keyword.VariableLevel1 or keyword.Term or keyword.Topic or keyword.Category


def anzsrc_uri(**kwargs):
    assert kwargs['data'] != None, "data not provided"
    assert kwargs['models'] != None, "models not provided"
    data = kwargs['data']
    models = kwargs['models']
    keyword = models['backend']['anzsrckeyword'].objects.get(pk=data)
    return keyword.uri


def identity(x):
    return x


def vocab_url(x):
    return x['vocabularyTermURL']


def is_orcid(x):
    # it's not an orcid
    if x and x[:4] == 'http':
        return False
    return True


def write_orcid(x):
    if is_orcid(x):
        return 'https://orcid.org/{orcid}'.format(orcid=x)
    else:
        return x


def write_orcid_role(x):
    if is_orcid(x):
        return "orcid"
    else:
        return "uri"


def write_constraints(x):
    return 'otherRestrictions'


def vocab_text(x):
    return x['term']


def sampling_text(x):
    return x['prefLabel']


def sampling_uri(x):
    return x['uri']


def write_doi(x):
    return 'doi: {doi}'.format(doi=x)


# TODO: should not be in common
def geonetwork_url(x):
    return 'https://geonetwork.tern.org.au/geonetwork/srv/eng/catalog.search#/metadata/{uuid}'.format(uuid=x)


# TODO: should not be in common
SPEC_FUNCTIONS = {
    "massage_version_number": massage_version_number,
    "new_term_vocab_prune": new_term_vocab_prune,
    "generate_attachment_url": generate_attachment_url,
    "create_linkage_uuid": create_linkage_uuid,
    "filename": filename,
    "prune_if_empty": prune_if_empty,
    "today": today,
    "parse_keywords": parse_keywords,
    "science_keyword_from_uuid": science_keyword_from_uuid,
    "date_as_string": date_as_string,
    "has_geographic_coverage": has_geographic_coverage,
    "to_string": to_string,
    "has_vertical_extent": has_vertical_extent,
    "vertical_crs_identifier": vertical_crs_identifier,
    "identity": identity,
    "person_uri": person_uri,
    "institution_uri": institution_uri,
    "parse_vertical_elevation": parse_vertical_elevation,
    "parse_vertical_method": parse_vertical_method,
    "parse_individual_name": parse_individual_name,
    "parse_individual_identifier": parse_individual_identifier,
    "parse_organisation_identifier": parse_organisation_identifier,
    "parse_individual_orcid": parse_individual_orcid,
    "science_keyword_name": science_keyword_name,
    "science_keyword_uri": science_keyword_uri,
    "anzsrc_keyword_name": anzsrc_keyword_name,
    "anzsrc_uri": anzsrc_uri,
    "parse_codeListValue": parse_codeListValue,
    "vocab_url": vocab_url,
    "vocab_text": vocab_text,
    "sampling_uri": sampling_uri,
    "sampling_text": sampling_text,
    "write_orcid": write_orcid,
    "write_orcid_role": write_orcid_role,
    "write_doi": write_doi,
    "geonetwork_url": geonetwork_url,
    "write_constraints": write_constraints,
    "separate_organisation_identifier": separate_organisation_identifier,
}
