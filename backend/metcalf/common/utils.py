import json

from lxml import etree

from metcalf.common import spec4, xmlutils5
from metcalf.common import xmlutils4


def to_json(x):
    if isinstance(x, str):
        return json.loads(x)
    # Else hope it's already json
    return x


def get_exception_message(e):
    return e.args[0] if len(e.args) == 1 else ''


def no_spaces_in_filename(instance, filename):
    return filename.replace(" ", "_")


def get_user_name(obj):
    if obj.email:
        return obj.email
    return obj.username


def create_export_xml_string(doc, uuid):
    data = to_json(doc.latest_draft.data)
    xml = etree.parse(doc.template.file.path)
    spec = spec4.make_spec(uuid=uuid, mapper=doc.template.mapper)
    xmlutils4.data_to_xml(data=data, xml_node=xml, spec=spec, nsmap=spec['namespaces'],
                          element_index=0, silent=True, fieldKey=None, doc_uuid=uuid)
    xmlutils5.export2(
        data=data,
        xml_node=xml,
        spec=spec,
        xml_kwargs={"namespaces": spec['namespaces']},
        handlers={
            "generateParameterUnitKeywords": xmlutils5.export2_generateParameterKeywords_handler,
            "generateDatasourceDistributions": xmlutils5.export2_generateDatasourceDistributions_handler,
        })
    return etree.tostring(xml)
