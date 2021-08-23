# TODO: Looks like common code

import datetime
import inspect
import logging
from copy import deepcopy
from decimal import Decimal
from django.apps import apps
from django.utils.six import string_types

logger = logging.getLogger(__name__)


class SpecialKeys:
    comment = '!docstring'
    function = 'function'
    namespaces = 'namespaces'
    nodes = 'nodes'
    xpath = 'xpath'
    export = 'export'
    attributes = 'attributes'
    container = 'container'
    parser = 'parser'
    exportTo = 'exportTo'
    keep = 'keep'
    default = 'default'
    removeWhen = 'removeWhen'
    template = 'template'
    postprocess = 'postprocess'
    valueChild = 'valueChild'
    deleteWhenEmpty = 'deleteWhenEmpty'

    @staticmethod
    def all_keys():
        return [a for a in dir(SpecialKeys) if not a.startswith('__')]


def get_xpath(spec):
    return spec[SpecialKeys.xpath]


def get_container(spec):
    return spec.get(SpecialKeys.container, get_xpath(spec))


def get_batch(spec):
    return spec['batch']


def get_required(spec):
    return spec.get('required', False)


def has_valueChild(spec):
    """
    Indicates that the value for a specific field is stored within a child element.
    For example <electronicMailAddress><CharacterString>person@domain.com</CharacterString></electronicMailAddress>
    :param spec: the spec
    :return: True if the element has the valueChild property set, False otherwise
    """
    return SpecialKeys.valueChild in spec


def get_valueChild(spec):
    return spec.get(SpecialKeys.valueChild, False)


def get_namespaces(spec):
    return spec[SpecialKeys.namespaces]


def has_namespaces(spec):
    return SpecialKeys.namespaces in spec


def is_many(spec):
    return spec.get('many', False)


def set_many(spec, is_many):
    spec['many'] = is_many


def is_required(spec):
    return spec.get('required', False)


def has_nodes(spec):
    return SpecialKeys.nodes in spec


def get_nodes(spec):
    return spec.get(SpecialKeys.nodes)


def get_initial(spec):
    return spec.get('initial', None)


def get_parser(spec):
    return spec.get('parser')


def has_parser(spec):
    return 'parser' in spec


def is_keep(spec):
    return spec.get('keep', True)


def get_attributes(spec):
    return spec[SpecialKeys.attributes]


def has_exportTo(spec):
    return 'exportTo' in spec


def get_exportTo(spec):
    return spec.get('exportTo')


def is_fanout(spec):
    return spec.get('fanout', False)


def is_batch(spec):
    return spec.get('batch', False)


def is_postprocess(spec):
    return SpecialKeys.postprocess in spec


def get_postprocess(spec):
    return spec.get(SpecialKeys.postprocess)


def is_export(spec):
    return spec.get(SpecialKeys.export, True)


def get_value_type(eles):
    try:
        return eles[0].getchildren()[0].tag
    except:
        return None


def is_deleteWhenEmpty(spec):
    return spec.get(SpecialKeys.deleteWhenEmpty, True)


def extract_fields(tree, spec, **kwargs):
    if has_namespaces(spec):
        kwargs[SpecialKeys.namespaces] = get_namespaces(spec)
    xpath = get_xpath(spec)
    elements = tree.xpath(xpath, **kwargs)

    if is_required(spec) or SpecialKeys.nodes in spec:
        assert len(
            elements) > 0, "We require at least one xpath match for required fields and all branches.\n{0}\n{1}".format(
            get_xpath(spec), elements)

    field = spec.copy()
    for special_key in SpecialKeys.all_keys():
        if special_key in field: del field[special_key]

    if is_many(spec):
        field['many'] = True
        field['initial'] = spec.get('initial', [])
        if has_nodes(spec):
            field['fields'] = {k: extract_fields(elements[0], v, **kwargs)
                               for k, v in get_nodes(spec).items()}
        else:
            field['type'] = get_value_type(elements)

    elif has_nodes(spec):
        for k, v in get_nodes(spec).items():
            field[k] = extract_fields(elements[0], v, **kwargs)

    else:
        field['type'] = get_value_type(elements)
        field['initial'] = get_initial(spec)

    return field


def value(element, **kwargs):
    assert kwargs['namespaces'] != None, "No namespaces were specified."
    """
    Extract value.  Either tagged or text.  Always one value.

    :param element:lxml.Element
    :return: a value
    """
    if element is None:
        raise Exception("Expected a valid element to extract value from")

    value_element = element.xpath('*', **kwargs)
    if value_element and len(value_element) == 1:
        value_element = value_element[0]
        value = value_element.text
        tag = value_element.tag

        gco = '{%s}' % kwargs['namespaces']['gco']

        if value is None:
            return None
        elif tag == "%sDateTime" % gco:
            return datetime.datetime.strptime(value, "%Y-%m-%dT%H:%M:%S")

        elif tag == "%sDate" % gco:
            return datetime.datetime.strptime(value, "%Y-%m-%d").date()

        elif tag == "%sDecimal" % gco:
            return Decimal(value)

        elif tag == "%sCharacterString" % gco:
            pass

        return value

    texts = element.xpath("text()")
    if texts is None:
        pass
    elif len(texts) == 1:
        return texts[0]
    elif len(texts) > 1:
        assert "Didn't expect multiple results to text() xpath query: %s" % element


def get_default(spec):
    default = spec.get('default', '')
    if hasattr(default, '__call__'):
        return default()
    else:
        return default


def process_node_child(element, spec, **kwargs):
    if has_nodes(spec):
        nodes = get_nodes(spec)
        if isinstance(nodes, dict):
            return {n: extract_xml_data(element, s, **kwargs)
                    for n, s in get_nodes(spec).items()}
        else:
            assert "Expected dict.  Got %s" % type(nodes)
    else:
        if is_keep(spec) and spec.get('default', None) is None:
            if has_parser(spec):
                return get_parser(spec)(element)
            else:
                return value(element, **kwargs)
        else:
            return get_default(spec)


def extract_xml_data(tree, spec, **kwargs):
    required = is_required(spec)
    initial = get_initial(spec)

    if has_namespaces(spec):
        kwargs['namespaces'] = get_namespaces(spec)

    elements = tree.xpath(get_xpath(spec), **kwargs)

    if not isinstance(elements, list):
        if is_keep(spec) and spec.get('default', None) is None:
            return elements
        else:
            return get_default(spec)

    if not is_many(spec):
        assert len(elements) < 2, \
            "XPath must resolve to single element:\n" \
            "element: %s\n" \
            "node: %s\n" \
            "elements: %s" % (tree, spec, elements)
    if is_many(spec):
        if is_keep(spec):
            return [process_node_child(element, spec, **kwargs) for element in elements]
        else:
            return []
    elif len(elements) == 0 and not required:
        return initial
    elif len(elements) == 1:
        return process_node_child(elements[0], spec, **kwargs)
    else:
        assert len(elements) > 0, ["No matches for required", get_xpath(spec), tree]


def parse_attributes(spec, namespaces):
    """
    Pull the attribute types/transforms from the spec, or 'text' and identity function if not specified
    :param spec: xml template spec
    :param namespaces: map of namespaces
    :return: dict with key attribute type value transform function
    """
    try:
        attrs = get_attributes(spec)
        namespaced_attrs = {}
        for attr, value in attrs.items():
            attrsplit = attr.split(':')
            if len(attrsplit) == 2:
                try:
                    namespaced_attr = '{%s}%s' % (namespaces[attrsplit[0]]
                                                  , attrsplit[1])
                    namespaced_attrs[namespaced_attr] = value
                except KeyError:
                    raise Exception('No matching namespace for attribute %s' % attr)
            else:
                namespaced_attrs[attr] = value
    except KeyError:
        return {'text': lambda x: x}
    return namespaced_attrs


def item_is_empty(data, k, v):
    return k not in data or data[k] is None or data[k] == '' or ('removeWhen' in v and v['removeWhen'](data[k]))


def spec_data_from_batch(batch_spec, key):
    assert isinstance(key, string_types), ("Expected a string key, but got {0}".format(type(key).__name__))
    data = {name: node['data'] for name, node in batch_spec[key].items()}
    spec = {SpecialKeys.xpath: '.', SpecialKeys.nodes: batch_spec[key]}
    return spec, data


# TODO: this is a workaround for the unusual structure of the geographic extents
# Basically each one needs to have its own mri:extent
# but the first one should have the start/end date and description
# this should be doable through the mapping/frontend but we don't
# have time.
# This takes any geographic extents beyond the first and shoves them into
# a geographicElementSecondary dict, which has a different xpath to the
# geographicElement, meaning we can write the two different types
def split_geographic_extents(data):
    geo = data['identificationInfo']['geographicElement']
    boxes = geo.get('boxes', None)
    if boxes:
        if len(boxes) > 1:
            data['identificationInfo']['geographicElementSecondary'] = []
        else:
            data['identificationInfo'].pop('geographicElementSecondary', None)
        for box in boxes[1:]:
            newBox = {}
            newBox['boxes'] = box
            data['identificationInfo']['geographicElementSecondary'].append(newBox)
        data['identificationInfo']['geographicElement']['boxes'] = [boxes[0]]
    return data


def data_to_xml(data, xml_node, spec, nsmap, doc_uuid, element_index=0, silent=True, fieldKey=None):
    # indicates that the spec allows more than one value for this node
    if is_many(spec):
        # sets many to false on the spec, because the spec is passed into the subsequent data_to_xml call
        set_many(spec, False)
        container_xpath = get_container(spec)
        container_node = xml_node.xpath(container_xpath, namespaces=nsmap)
        if is_fanout(spec):
            for i in range(len(container_node)):
                data_to_xml(data=data, xml_node=xml_node, spec=spec, nsmap=nsmap,
                            element_index=i, silent=silent, fieldKey=fieldKey, doc_uuid=doc_uuid)
        else:
            if len(container_node) < 1:
                msg = "container at xpath %s is not found" % container_xpath
                if silent:
                    logger.warning(msg)
                    return
                else:
                    raise Exception(msg)
            mount_node = container_node[0].getparent()
            mount_index = mount_node.index(container_node[0])
            template = deepcopy(container_node[0])
            # remove any existing entries from the node
            for element in container_node:
                mount_node.remove(element)
            # call data_to_xml once for each item in the data
            for i, item in enumerate(data):
                mount_node.insert(mount_index + i, deepcopy(template))
                data_to_xml(data=item, xml_node=xml_node, spec=spec, nsmap=nsmap,
                            element_index=i, silent=silent, fieldKey=fieldKey, doc_uuid=doc_uuid)

    # export can be false with an exportTo function, i.e. don't do the default export, do this instead
    elif not is_export(spec):
        if has_exportTo(spec):
            data_to_xml(data=data, xml_node=xml_node, spec=get_exportTo(spec), nsmap=nsmap,
                        element_index=element_index, silent=silent, fieldKey=fieldKey, doc_uuid=doc_uuid)
    elif is_batch(spec):
        spec, data = spec_data_from_batch(get_batch(spec), data)
        data_to_xml(data=data, xml_node=xml_node, spec=spec, nsmap=nsmap,
                    element_index=0, silent=silent, fieldKey=fieldKey, doc_uuid=doc_uuid)
    elif has_nodes(spec):
        xml_node = xml_node.xpath(get_xpath(spec), namespaces=nsmap)[element_index]
        for field_key, node_spec in get_nodes(spec).items():
            # workaround for a problem with identifiers in the final output
            # we need to write either the orcid or the uri to the XML file
            # but we can't do that at the node writing point, because we don't have the
            # sibling data
            # TODO: there is a better way to structure this, but we can't overhaul the mapper right now
            if field_key == 'orcid':
                orcid = data[field_key]
                if not orcid:
                    data[field_key] = data['uri']
            if item_is_empty(data, field_key, node_spec):
                if get_required(node_spec):
                    # at the moment, we are always graceful to missing fields, only reporting them w/o raising exception
                    logger.warning('%s field is required, but missing' % field_key)
                container_xpath = get_container(node_spec)
                # don't delete it, but do have to clear any preset value
                if not is_deleteWhenEmpty(node_spec):
                    elements = xml_node.xpath(container_xpath, namespaces=nsmap)
                    for element in elements:
                        element.text = ''
                elif container_xpath is not None:
                    elements = xml_node.xpath(container_xpath, namespaces=nsmap)
                    for element in elements:
                        element.getparent().remove(element)
                continue
            data_to_xml(data=data[field_key], xml_node=xml_node, spec=node_spec, nsmap=nsmap,
                        element_index=0, silent=silent, fieldKey=field_key, doc_uuid=doc_uuid)
    # default behaviour; populate the xml elements with the values in the data
    else:
        node_xpath = get_xpath(spec)
        is_attr = node_xpath.startswith('@')
        if is_attr:
            attr_split = node_xpath[1:].split(':')
            attr_ns = attr_split[0]
            attr_name = attr_split[1]
            xml_node.set('{%s}%s' % (nsmap[attr_ns], attr_name), data)
        else:
            elements = xml_node.xpath(node_xpath, namespaces=nsmap)

            if len(elements) < element_index + 1:
                msg = 'element %s[%d] not found in template, not written' % (get_xpath(spec), element_index)
                if silent:
                    logger.warning(msg)
                else:
                    raise Exception(msg)
                return
            element = elements[element_index]
            if has_valueChild(spec):
                # xpath returns a list regardless, but we require only one result
                valueChildren = element.xpath(get_valueChild(spec), namespaces=nsmap)
                if len(valueChildren) == 1:
                    element = valueChildren[0]
                else:
                    msg = 'element %s had an incorrect number of valueChild results (%d)' % (
                    get_xpath(spec), len(valueChildren))
                    if silent:
                        logger.warning(msg)
                    else:
                        raise Exception(msg)
                    return
            # at this point we should have a single node to deal with
            if len(element.getchildren()) > 0:
                msg = 'element %s had children' % (get_xpath(spec))
                if silent:
                    logger.warning(msg)
                else:
                    raise Exception(msg)
                return
            for attr, transform in parse_attributes(spec, nsmap).items():
                transform_sig = inspect.getfullargspec(transform)
                is_kwargs = transform_sig[2] is not None
                arity = len(transform_sig[0])
                if is_kwargs:
                    final_value = transform(data=data, models=apps.all_models, uuid=doc_uuid)
                elif arity == 1:
                    final_value = transform(data)
                elif arity == 2:
                    source_value = value(element, namespaces=nsmap)
                    final_value = transform(data, source_value)
                else:
                    msg = 'attr %s in spec %s has unsupported arity %d' % (attr, str(spec), arity)
                    if silent:
                        logger.warning(msg)
                        continue
                    else:
                        raise Exception(msg)
                if attr == 'text':
                    gco = '{%s}' % nsmap['gco']
                    # TODO: this only works if we don't care about actual time information
                    if element.tag == '%sDateTime' % gco:
                        element.text = '%sT00:00:00' % final_value
                    else:
                        element.text = final_value
                else:
                    element.set(attr, final_value)
            if has_exportTo(spec):
                data_to_xml(data=data, xml_node=xml_node, spec=get_exportTo(spec), nsmap=nsmap,
                            element_index=element_index, silent=silent, fieldKey=fieldKey, doc_uuid=doc_uuid)

    if is_postprocess(spec):
        get_postprocess(spec)(data, xml_node, spec, nsmap, element_index, silent)
