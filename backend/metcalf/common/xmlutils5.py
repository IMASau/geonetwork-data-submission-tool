from collections import defaultdict
import copy
import uuid
import math

import metcalf.common.xmlutils4 as xmlutils4


def get_items(spec):
    return spec.get('items')


def get_properties(spec):
    return spec.get('properties')


def is_object(spec):
    return spec.get('type', None) == 'object'


def extract2_not_empty_parser(nodes, **kwargs):
    return True, len(nodes) > 0


def extract2_text_string_parser(nodes, xpath, **kwargs):
    if len(nodes) == 0:
        return False, None
    elif len(nodes) == 1:
        text = nodes[0]
        return True, text
    else:
        raise Exception('Multiple xpath matches for value %s' % {'xpath': xpath, 'nodes': nodes})


def extract2_text_number_parser(nodes, xpath, **kwargs):
    if len(nodes) == 0:
        return False, None
    elif len(nodes) == 1:
        text = nodes[0]
        ret = float(text)
        if math.isnan(ret):
            raise Exception('Unable to parse value as number %s' % {'xpath': xpath, 'nodes': nodes, 'value': text})
        else:
            return True, ret
    else:
        raise Exception('Multiple xpath matches for value %s' % {'xpath': xpath, 'nodes': nodes})


def extract2(tree, spec, parsers, **kwargs):
    """
    Traverse spec and extracts data from xml tree.

    Operates based annotations
    - extract2_leaf or extract2_branch (mutually exclusive)
    - extract2_default

    extract2_leaf
    - Must include 'xpath' and 'parser'
    - Uses xpath to find nodes
    - Uses parser to lookup handler in 'parsers' registry
    - Calls handler with kwargs (nodes, xpath, spec).  Should return (hit, val) tuple.

    extract2_branch
    - Must include 'xpath'
    - Finds nodes using xpath
    - Iterates over nodes, passing as tree to recursive calls

    extract2_default
    - Defines a default value
    - Returned if no value is extracted

    Object
    - No annotations
    - Passes tree to recursive call
    - Properties which return data are included

    :param tree: Element used to query xpaths and extract values
    :param spec: Spec being traversed
    :param parsers: registry of parser handlers
    :param kwargs: Additional context passed to ele.xpath (namespaces...)
    :return: hit, data - hit indicates presence of extracted data value
    """

    extract2_leaf = spec.get('extract2_leaf', None)
    extract2_branch = spec.get('extract2_branch', None)
    has_default = 'extract2_default' in spec
    default = spec.get('extract2_default', None)

    assert not (extract2_branch and extract2_leaf), "Spec can't have both extract2_leaf and extract2_branch set"

    if extract2_leaf:
        xpath = extract2_leaf.get('xpath', None)
        parser = extract2_leaf.get('parser', None)

        assert xpath, "extract2_leaf xpath required"
        assert parser, "extract2_leaf parser required"

        handler = parsers.get(parser, None)

        assert handler, "extract2_leaf parser must resolve to handler"

        nodes = tree.xpath(xpath, **kwargs)
        parser_kwargs = {"nodes": nodes, "xpath": xpath, "spec": spec}
        hit, value = handler(**parser_kwargs)

        if hit:
            return hit, value

    elif extract2_branch:
        xpath = extract2_branch.get('xpath', None)
        items_spec = get_items(spec)

        assert xpath, "extract2_branch xpath required"
        assert items_spec, "extract2_branch spec items required"

        nodes = tree.xpath(xpath, **kwargs)
        hits = False
        ret = []
        for node in nodes:
            hit, data = extract2(node, items_spec, parsers, **kwargs)
            hits = hits or hit
            if hit:
                ret.append(data)

        if hits:
            return True, ret

    elif is_object(spec):
        ret = {}
        hits = False
        for prop_name, prop_spec in get_properties(spec).items():
            hit, data = extract2(tree, prop_spec, parsers, **kwargs)
            if hit:
                hits = True
                ret[prop_name] = data

        if hits:
            return True, ret

    if has_default:
        return True, default
    else:
        return False, None


def export2_xform(xform, data, xml_node, spec, xml_kwargs, handlers):
    handler = handlers.get(xform[0])
    assert handler, "export2: xf_name must resolve to handler"
    handler_kwargs = {
        "data": data,
        "xml_node": xml_node,
        "spec": spec,
        "xml_kwargs": xml_kwargs,
        "handlers": handlers,
        "xform": xform
    }
    handler(**handler_kwargs)


def export2(data, xml_node, spec, xml_kwargs, handlers):
    export2_xforms = spec.get('export2_xforms', [])
    for xform in export2_xforms:
        export2_xform(xform, data, xml_node, spec, xml_kwargs, handlers)


def get_data_path(data, path):
    if len(path) == 0:
        return True, data
    elif len(path) == 1:
        if data is not None and path[0] in data:
            return True, data[path[0]]
        else:
            return False, None
    else:
        if data is not None and path[0] in data:
            return get_data_path(data[path[0]], path[1:])
        else:
            return False, None


def get_spec_path(spec, prop_path):
    ret = spec
    for k in prop_path:
        ret = ret['properties'][k]
    return ret


def get_dotted_path(data, dotpath):
    assert dotpath is not None, "get_dotpath dotpath is required"
    path = dotpath.split('.')
    return get_data_path(data, path)


def export2_set_text_handler(data, xml_node, xml_kwargs, xform, **kwargs):
    """
    Set text for node if data is present.

    Configured with xform
    - xform[1].data_path to get data from
    - xform[1].node_xpath to node being updated

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    node_xpath = xf_props.get('node_xpath', None)
    assert data_path is not None, "export2_set_text_handler: xf_props.data_path must be set"
    assert node_xpath is not None, "export2_set_text_handler: xf_props.node_xpath must be set"
    hit, value = get_dotted_path(data, data_path)
    if hit:
        nodes = xml_node.xpath(node_xpath, **xml_kwargs)
        assert len(nodes) == 1, "node_xpath must match one node, %s found" % len(nodes)
        nodes[0].text = str(value)


def export2_remove_element_handler(data, xml_node, xml_kwargs, xform, **kwargs):
    """
    Remove matching elements if data not present.

    Configured with xf_props
    - xform[1].data_path for data presence check
    - xform[1].xpath to node which would be removed

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    node_xpath = xf_props.get('node_xpath', None)
    assert data_path is not None, "data_path must be set"
    assert node_xpath is not None, "node_xpath must be set"
    hit, value = get_dotted_path(data, data_path)
    if not hit:
        nodes = xml_node.xpath(xpath, **xml_kwargs)
        for node in nodes:
            node.getparent().remove(node)


def export2_append_items_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Append elements for each list item.

    Configured with xf_props
    - xform[1].data_path
    - xform[1].mount_xpath
    - xform[1].template_xpath

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    mount_xpath = xf_props.get('mount_xpath', None)
    template_xpath = xf_props.get('template_xpath', None)

    assert data_path is not None, "export2_append_items_handler: xf_props.data_path must be set"
    assert mount_xpath is not None, "export2_append_items_handler: xf_props.mount_xpath must be set"
    assert template_xpath is not None, "export2_append_items_handler: xf_props.template_xpath must be set"

    hit, values = get_dotted_path(data, data_path)
    mount_nodes = xml_node.xpath(mount_xpath, **xml_kwargs)
    template_nodes = xml_node.xpath(template_xpath, **xml_kwargs)

    assert len(mount_nodes) == 1, 'mount_xpath must match one node, %s found' % len(mount_nodes)
    assert len(template_nodes) > 0, 'template_xpath must match at least one node'

    template = template_nodes[0]

    if hit:
        items_spec = get_spec_path(spec, data_path.split('.'))['items']
        for value in values:
            element = copy.deepcopy(template)
            mount_nodes[0].append(element)
            item_xforms = xform[2:]
            for item_xform in item_xforms:
                export2_xform(
                    xform=item_xform,
                    data=value,
                    xml_node=element,
                    spec=items_spec,
                    xml_kwargs=xml_kwargs,
                    handlers=handlers)

        template.getparent().remove(template)


def export2_generateParameterKeywords_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Append keyword,unit pairing for each parameter.

    Configured with xf_props
    - xform[1].mount_xpath
    - xform[1].template_xpath
    - xform[1].data_path
    - xform[1].parameter_path (key under main data path)
    - xform[1].unit_path (key under main data path)

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    mount_xpath = xf_props.get('mount_xpath', None)
    template_xpath = xf_props.get('template_xpath', None)
    parameter_path = xf_props.get('parameter_path', None)
    unit_path = xf_props.get('unit_path', None)

    assert mount_xpath is not None, "export2_generateParameterKeywords_handler: xf_props.mount_xpath must be set"
    assert template_xpath is not None, "export2_generateParameterKeywords_handler: xf_props.template_xpath must be set"
    assert data_path is not None, "export2_generateParameterKeywords_handler: xf_props.data_path must be set"
    assert parameter_path is not None, "export2_generateParameterKeywords_handler: xf_props.parameter_path must be set"
    assert unit_path is not None, "export2_generateParameterKeywords_handler: xf_props.unit_path must be set"

    hit, values = get_dotted_path(data, data_path)
    mount_nodes = xml_node.xpath(mount_xpath, **xml_kwargs)
    template_nodes = xml_node.xpath(template_xpath, **xml_kwargs)

    assert len(mount_nodes) == 1
    assert len(template_nodes) >= 1

    mount_node = mount_nodes[0]
    mount_index = mount_node.index(template_nodes[0])
    template = copy.deepcopy(template_nodes[0])
    for node in template_nodes:
        node.getparent().remove(node)

    if hit:
        items_spec = get_spec_path(spec, data_path.split('.'))['items']['properties']

        i = 0
        for value in values:
            # Need to do this twice; once for parameter, once for unit (each has different title)
            for key,title in [(parameter_path, 'Parameter'), (unit_path, 'UOM')]:
                data = value[key]
                element = copy.deepcopy(template)
                # FIXME: Could make this generic like data_to_xml, but
                # that's a bit messy so just hard-code for now:
                anchor = element.xpath('gcx:Anchor', **xml_kwargs)[0]
                for prop, propspec in items_spec[key]['properties'].items():
                    if prop not in data or 'xpath' not in propspec:
                        continue
                    attrval = data[prop]
                    attrs = xmlutils4.parse_attributes(propspec, xml_kwargs['namespaces'])
                    for attr, f in attrs.items():
                        if attr == 'text':
                            anchor.text = f(attrval)
                        else:
                            anchor.set(attr, f(attrval))
                # hard-code the title:
                anchor.set('{http://www.w3.org/1999/xlink}title', title)
                mount_node.insert(mount_index + i, element)
                i += 1


def export2_imasGenerateKeywords_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Append keyword for each parameter.

    Configured with xf_props
    - data_path - path to parameter table
    - term_key - key in parameter table which has: Name, URI and optionally isUserDefined
    - mount_xpath - xpath where elements will be mounted
    - template_xpath - xpath to template.  First is used as a template.  All matches are removed.

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    term_key = xf_props.get('term_key', None)
    mount_xpath = xf_props.get('mount_xpath', None)
    template_xpath = xf_props.get('template_xpath', None)

    assert data_path is not None, "data_path must be set"
    assert term_key is not None, "term_key must be set"
    assert mount_xpath is not None, "mount_xpath must be set"
    assert template_xpath is not None, "template_xpath must be set"

    hit, values = get_dotted_path(data, data_path)
    mount_nodes = xml_node.xpath(mount_xpath, **xml_kwargs)
    template_nodes = xml_node.xpath(template_xpath, **xml_kwargs)
    nsmap = xml_kwargs['namespaces']
    attr = '{%s}%s' % (nsmap['xlink'], 'href')

    assert len(mount_nodes) == 1, "A single mount element is required"
    assert len(template_nodes) >= 1, "At least one template node is required"

    mount_node = mount_nodes[0]
    mount_index = mount_node.index(template_nodes[0])

    # Prepare a template
    template = copy.deepcopy(template_nodes[0])

    # Delete all template nodes
    for node in template_nodes:
        node.getparent().remove(node)

    if hit:

        seen = set()

        # Add a keyword for each term
        for i, value in enumerate(values):
            term = value.get(term_key)

            # Skip optional, unset terms
            if term is None:
                continue

            is_user_defined = term.get('isUserDefined', False)
            name = term.get('Name')
            uri = term.get('URI')

            assert name is not None
            assert uri is not None

            if uri in seen:
                continue
            else:
                seen.add(uri)

            element = copy.deepcopy(template)
            anchor = element.xpath('gcx:Anchor', **xml_kwargs)[0]

            if is_user_defined:
                anchor.text = name
                anchor.attrib.pop(attr)
            else:
                assert name is not None
                anchor.text = name
                anchor.set(attr, uri)

            mount_node.insert(mount_index + i, element)
            i += 1


def export2_imasParameterUnitAttributeGroup_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Add attribute group for parameter/unit combinations.

    Configured with xf_props
    - data_path - path to parameter table
    - mount_xpath - xpath where elements will be mounted
    - template_xpath - xpath to template.  First is used as a template.  All matches are removed.

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    mount_xpath = xf_props.get('mount_xpath', None)
    template_xpath = xf_props.get('template_xpath', None)

    assert data_path is not None, "data_path must be set"
    assert mount_xpath is not None, "mount_xpath must be set"
    assert template_xpath is not None, "template_xpath must be set"

    hit, values = get_dotted_path(data, data_path)
    mount_nodes = xml_node.xpath(mount_xpath, **xml_kwargs)
    template_nodes = xml_node.xpath(template_xpath, **xml_kwargs)

    assert len(mount_nodes) == 1, "A single mount element is required, %s found" % (len(mount_nodes))
    assert len(template_nodes) >= 1, "At least one template node is required, %s found" % (len(template_nodes))

    nsmap = xml_kwargs['namespaces']
    xlink_href_attr = '{%s}%s' % (nsmap['xlink'], 'href')

    mount_node = mount_nodes[0]
    mount_index = mount_node.index(template_nodes[0])

    # Prepare a template
    template = copy.deepcopy(template_nodes[0])

    # Delete all template nodes
    for node in template_nodes:
        node.getparent().remove(node)

    if hit:

        # Add a keyword for each term
        for i, value in enumerate(values):
            longName_term = value.get('longName_term')
            name = value.get('name')
            unit_term = value.get('unit_term')

            # Skip optional, unset terms
            if longName_term is None:
                continue

            if unit_term is None:
                continue

            # TODO: duplicate detection

            element = copy.deepcopy(template)

            # Parameter #1
            param_xpath = 'mrc:MD_SampleDimension/mrc:name/mcc:MD_Identifier/mcc:code/gcx:Anchor'
            param_node = element.xpath(param_xpath, **xml_kwargs)[0]

            if longName_term.get('isUserDefined'):
                param_node.text = longName_term.get('Name')
                param_node.attrib.pop(xlink_href_attr)
            else:
                param_node.text = longName_term.get('Name')
                param_node.set(xlink_href_attr, longName_term.get('URI'))

            # Second 'freetext' instance of name (ie name in dataset), if entered
            name_xpath = 'mrc:MD_SampleDimension/mrc:name[mcc:MD_Identifier/mcc:code/gco:CharacterString]'
            name_root = element.xpath(name_xpath, **xml_kwargs)[0]
            name_node = name_root.xpath('mcc:MD_Identifier/mcc:code/gco:CharacterString', **xml_kwargs)[0]

            if name:
                name_node.text = name
            else:
                name_root.getparent().remove(name_root)

            # Unit #1
            unit_identifier_xpath = 'mrc:MD_SampleDimension/mrc:units/gml:BaseUnit/gml:identifier'
            unit_identifier_node = element.xpath(unit_identifier_xpath, **xml_kwargs)[0]
            unit_name_node = element.xpath('mrc:MD_SampleDimension/mrc:units/gml:BaseUnit/gml:name', **xml_kwargs)[0]

            if unit_term.get('isUserDefined'):
                unit_name_node.text = unit_term.get('Name')
                unit_identifier_node.getparent().remove(unit_identifier_node)
            else:
                unit_name_node.text = unit_term.get('Name')
                unit_identifier_node.text = unit_term.get('URI')

            mount_node.insert(mount_index + i, element)
            i += 1


def export2_imasParameterInstrumentAcquisitionInformation_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Add acquisition information for parameter/instrument combinations.

    Configured with xf_props
    - data_path - path to parameter table
    - mount_xpath - xpath where elements will be mounted
    - template_xpath - xpath to template.  First is used as a template.  All matches are removed.

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    mount_xpath = xf_props.get('mount_xpath', None)
    template_xpath = xf_props.get('template_xpath', None)

    assert data_path is not None, "data_path must be set"
    assert mount_xpath is not None, "mount_xpath must be set"
    assert template_xpath is not None, "template_xpath must be set"

    hit, values = get_dotted_path(data, data_path)
    mount_nodes = xml_node.xpath(mount_xpath, **xml_kwargs)
    template_nodes = xml_node.xpath(template_xpath, **xml_kwargs)

    assert len(mount_nodes) == 1, "A single mount element is required, %s found" % (len(mount_nodes))
    assert len(template_nodes) >= 1, "At least one template node is required, %s found" % (len(template_nodes))

    nsmap = xml_kwargs['namespaces']
    xlink_href_attr = '{%s}%s' % (nsmap['xlink'], 'href')

    mount_node = mount_nodes[0]
    mount_index = mount_node.index(template_nodes[0])

    # Prepare a template
    template = copy.deepcopy(template_nodes[0])

    # Delete all template nodes
    for node in template_nodes:
        node.getparent().remove(node)

    if hit:

        platforms = {}
        platform_instruments = defaultdict(list)
        for value in values:
            platform_term = value.get('platform_term')
            instrument_term = value.get('instrument_term')

            if platform_term:
                platforms[platform_term['URI']] = platform_term

            if platform_term and instrument_term:
                platform_instruments[platform_term['URI']].append(instrument_term)

        # Add a keyword for each term
        for i, platform_uri in enumerate(platform_instruments):
            platform = platforms[platform_uri]
            instruments = platform_instruments[platform_uri]

            if len(instruments) == 0:
                continue

            element = copy.deepcopy(template)

            # Platform
            platform_xpath = "mac:MI_Platform/mac:identifier/mcc:MD_Identifier/mcc:code/gcx:Anchor"
            platform_node = element.xpath(platform_xpath, **xml_kwargs)[0]

            if platform.get('isUserDefined'):
                platform_node.text = platform.get('Name')
                platform_node.attrib.pop(xlink_href_attr)
            else:
                platform_node.text = platform.get('Name')
                platform_node.set(xlink_href_attr, platform.get('URI'))

            # Multiple instruments per platform
            instrument_mount_xpath = "mac:MI_Platform[mac:instrument]"
            instrument_template_xpath = "mac:MI_Platform/mac:instrument"

            instrument_mount_nodes = element.xpath(instrument_mount_xpath, **xml_kwargs)
            instrument_template_nodes = element.xpath(instrument_template_xpath, **xml_kwargs)

            instrument_mount_node = instrument_mount_nodes[0]
            instrument_mount_index = instrument_mount_node.index(instrument_template_nodes[0])

            # Prepare template
            instrument_template = copy.deepcopy(instrument_template_nodes[0])
            for node in instrument_template_nodes:
                node.getparent().remove(node)

            for j, instrument in enumerate(instruments):

                instrument_element = copy.deepcopy(instrument_template)

                instrument_xpath = "mac:MI_Instrument/mac:identifier/mcc:MD_Identifier/mcc:code/gcx:Anchor"
                instrument_node = instrument_element.xpath(instrument_xpath, **xml_kwargs)[0]

                if instrument.get('isUserDefined'):
                    instrument_node.text = instrument.get('Name')
                    instrument_node.attrib.pop(xlink_href_attr)
                else:
                    instrument_node.text = instrument.get('Name')
                    instrument_node.set(xlink_href_attr, instrument.get('URI'))

                instrument_mount_node.insert(instrument_mount_index + j, instrument_element)
                j += 1

            mount_node.insert(mount_index + i, element)
            i += 1


imasLegalConstraintsLookup = {
    'CC-BY': {
        "uri": "https://creativecommons.org/licenses/by/4.0/",
        "title": "Creative Commons Attribution 4.0 International License",
        "alt_title": "CC-BY",
        "edition": "4.0",
        "graphic": "https://licensebuttons.net/l/by/4.0/88x31.png"
    },
    'CC-BY-NC': {
        "uri": "https://creativecommons.org/licenses/by-nc/4.0/",
        "title": "Creative Commons Attribution-NonCommercial 4.0 International License",
        "alt_title": "CC-BY-NC",
        "edition": "4.0",
        "graphic": "https://licensebuttons.net/l/by-nc/4.0/88x31.png"}
}


def set_text_helper(xml_node, xpath, data, xml_kwargs):
    nodes = xml_node.xpath(xpath, **xml_kwargs)
    assert len(nodes) == 1, "Expected one node, found %s" % len(nodes)
    nodes[0].text = str(data)


def remove_element_helper(xml_node, xpath, xml_kwargs):
    nodes = xml_node.xpath(xpath, **xml_kwargs)
    assert len(nodes) == 1, "Expected one node to remove, found %s" % len(nodes)
    for node in nodes:
        node.getparent().remove(node)


def export2_imasLegalConstraints_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    xf_props = xform[1]
    node_xpath = xf_props.get('node_xpath', None)

    cc_hit, creative_commons = get_dotted_path(data, 'identificationInfo.creativeCommons')
    oc_hit, other_constraints = get_dotted_path(data, 'identificationInfo.otherConstraints')

    if cc_hit:
        cc_value = creative_commons.get('value')
        constraints_key = 'CC-BY' if cc_value == 'OTHER' else cc_value
        constraints = imasLegalConstraintsLookup.get(constraints_key)

        nodes = xml_node.xpath(node_xpath, **xml_kwargs)
        assert len(nodes) == 1

        graphic_xpath = "mco:MD_LegalConstraints/mco:graphic/mcc:MD_BrowseGraphic/mcc:linkage/cit:CI_OnlineResource/cit:linkage/gco:CharacterString"
        title_xpath = "mco:MD_LegalConstraints/mco:reference/cit:CI_Citation/cit:title/gco:CharacterString"
        alt_title_xpath = "mco:MD_LegalConstraints/mco:reference/cit:CI_Citation/cit:alternateTitle/gco:CharacterString"
        edition_xpath = "mco:MD_LegalConstraints/mco:reference/cit:CI_Citation/cit:edition/gco:CharacterString"
        uri_xpath = "mco:MD_LegalConstraints/mco:reference/cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString"
        other_root_xpath = "mco:MD_LegalConstraints/mco:otherConstraints[2]"
        other_value_xpath = "mco:MD_LegalConstraints/mco:otherConstraints[2]/gco:CharacterString"

        set_text_helper(xml_node=nodes[0], xpath=graphic_xpath, data=constraints['graphic'], xml_kwargs=xml_kwargs)
        set_text_helper(xml_node=nodes[0], xpath=title_xpath, data=constraints['title'], xml_kwargs=xml_kwargs)
        set_text_helper(xml_node=nodes[0], xpath=alt_title_xpath, data=constraints['alt_title'], xml_kwargs=xml_kwargs)
        set_text_helper(xml_node=nodes[0], xpath=edition_xpath, data=constraints['edition'], xml_kwargs=xml_kwargs)
        set_text_helper(xml_node=nodes[0], xpath=uri_xpath, data=constraints['uri'], xml_kwargs=xml_kwargs)

        if other_constraints:
            set_text_helper(xml_node=nodes[0], xpath=other_value_xpath, data=other_constraints, xml_kwargs=xml_kwargs)
        else:
            remove_element_helper(xml_node=nodes[0], xpath=other_root_xpath, xml_kwargs=xml_kwargs)

    else:
        remove_element_helper(xml_node=xml_node, xpath=node_xpath, xml_kwargs=xml_kwargs)


def export2_imasDigitalTransferOptions_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Massages data from distributionInfo.transferOptions and identificationInfo.supportingResources to
    generate a list of Digital Transfer Options.

    :param data:
    :param xml_node:
    :param spec:
    :param xml_kwargs:
    :param handlers:
    :param xform:
    :return:
    """
    xf_props = xform[1]
    mount_xpath = xf_props.get('mount_xpath', None)
    template_xpath = xf_props.get('template_xpath', None)

    to_hit, to_data = get_dotted_path(data, "distributionInfo.transferOptions")
    sr_hit, sr_data = get_dotted_path(data, "identificationInfo.supportingResources")

    dtos = [*to_data] if to_hit else []

    if sr_hit:
        for sr in sr_data:
            dtos.append({
                "description": sr.get('name'),
                "linkage": sr.get('url'),
                "protocol": 'WWW:LINK-1.0-http--downloaddata',
            })

    export2_append_items_handler(
        data={'dtos': dtos},
        xml_node=xml_node,
        spec={
            "type": "object",
            "properties": {
                "dtos": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "description": {"type": "string"},
                            "linkage": {"type": "string"},
                            "protocol": {"type": "string"},
                            "name": {"type": "string"}
                        }
                    }
                }
            }
        },
        xml_kwargs=xml_kwargs,
        handlers=handlers,
        xform=[
            "append_items",
            {
                "data_path": "dtos",
                "mount_xpath": mount_xpath,
                "template_xpath": template_xpath
            },
            *xform[2]
        ]
    )


def export2_generateUnitKeywords_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Append keyword for each unit (contained as a sub-child of parameter).

    Configured with xf_props
    - xform[1].data_path
    - xform[1].mount_xpath
    - xform[1].template_xpath

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    mount_xpath = xf_props.get('mount_xpath', None)
    template_xpath = xf_props.get('template_xpath', None)

    assert data_path is not None, "export2_generateUnitKeywords_handler: xf_props.data_path must be set"
    assert mount_xpath is not None, "export2_generateUnitKeywords_handler: xf_props.mount_xpath must be set"
    assert template_xpath is not None, "export2_generateUnitKeywords_handler: xf_props.template_xpath must be set"

    hit, values = get_dotted_path(data, data_path)
    mount_nodes = xml_node.xpath(mount_xpath, **xml_kwargs)
    template_nodes = xml_node.xpath(template_xpath, **xml_kwargs)

    assert len(mount_nodes) == 1
    assert len(template_nodes) >= 1

    mount_node = mount_nodes[0]
    mount_index = mount_node.index(template_nodes[0])
    template = copy.deepcopy(template_nodes[0])
    for node in template_nodes:
        node.getparent().remove(node)

    if hit:
        items_spec = get_spec_path(spec, data_path.split('.'))['items']['properties']

        i = 0
        for value in values:
            # FIXME: We should always have a unit, but handle the case
            # where we don't for robustness:
            if 'unit' not in value:
                continue
            uval = value['unit']
            element = copy.deepcopy(template)
            # FIXME: Could make this generic like data_to_xml, but
            # that's a bit messy so just hard-code for now.
            anchor = element.xpath('gcx:Anchor', **xml_kwargs)[0]
            anchor.text = uval['label']
            attrs = xmlutils4.parse_attributes(items_spec['uri'], xml_kwargs['namespaces'])
            for attr, f in attrs.items():
                anchor.set(attr, f(uval['uri']))
            mount_node.insert(mount_index + i, element)
            i += 1


def export2_generateDatasourceDistributions_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Append keyword for each unit (contained as a sub-child of parameter).

    Configured with xf_props
    - xform[1].data_path
    - xform[1].template_xpath

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    template_xpath = xf_props.get('template_xpath', None)

    assert data_path is not None, "export2_generateDatasourceDistributions_handler: xf_props.data_path must be set"
    assert template_xpath is not None, "export2_generateDatasourceDistributions_handler: xf_props.template_xpath must be set"

    hit, values = get_dotted_path(data, data_path)
    template_nodes = xml_node.xpath(template_xpath, **xml_kwargs)

    assert len(template_nodes) >= 1

    mount_node = template_nodes[0].getparent()
    mount_index = mount_node.index(template_nodes[0])
    template = copy.deepcopy(template_nodes[0])
    for node in template_nodes:
        node.getparent().remove(node)

    if hit:
        items_spec = get_spec_path(spec, data_path.split('.'))['items']['properties']

        groupby = defaultdict(list)
        for v in values:
            groupby[v['distributor']['uri']].append(v)

        distIdx = 0
        # For each distibutor:
        for sources in groupby.values():
            # FIXME: the "nicer" way to do this is probably to
            # reformat the spec (ie, wrap components as
            # objects/arrays), then hand down to xmlutils4.data_to_xml
            # again for export, which handles all the corner cases
            # already.  See below for a small part of this.
            distributor = sources[0]['distributor']
            distributionNode = copy.deepcopy(template)
            distributorSpec = items_spec['distributor']
            distributorXpath = distributorSpec['xpath']
            distributorSpec = distributorSpec['properties']

            # populate the distibutor elements (address, etc)
            for prop, spec in distributorSpec.items():
                if 'xpath' not in spec:
                    continue
                specxpath = spec["xpath"]
                path = f"{distributorXpath}/{specxpath}"
                node = distributionNode.xpath(path, **xml_kwargs)
                assert len(node) == 1, f"Expected a single node for {path}, found {len(node)}"
                node = node[0]
                if prop not in distributor:
                    node.getparent().remove(node)
                    continue
                if 'valueChild' in spec:
                    path = spec['valueChild']
                    node = node.xpath(path, **xml_kwargs)[0]
                attrs = xmlutils4.parse_attributes(spec, xml_kwargs['namespaces'])
                for attr, transform in attrs.items():
                    if attr == 'text':
                        node.text = transform(distributor[prop])
                    else:
                        node.set(attr, transform(distributor[prop]))

            # Now populate each transferOptions item, as children after the distributor info:
            transferSpec = items_spec['transferOptions']
            transferXpath = transferSpec['xpath']
            transferSpec = transferSpec['properties']
            # clone the transferOptions node, use as template
            transferTemplate = distributionNode.xpath(transferXpath, **xml_kwargs)
            assert len(
                transferTemplate) == 1, f"Expected a single node for {transferXpath}, found {len(transferTemplate)}"
            transferTemplate = transferTemplate[0]
            transferMount = transferTemplate.getparent()
            transferIdx = transferMount.index(transferTemplate)
            transferMount.remove(transferTemplate)

            nsmap = xml_kwargs['namespaces']
            # Wrap the current spec as an object, so we can use xmlutils4.data_to_xml again:
            transferSpecObject = {
                "type": "object",
                "xpath": ".",
                "properties": transferSpec,
            }
            for i, source in enumerate(sources):
                # write transferOptions:
                transferOptions = source['transferOptions']
                transferNode = copy.deepcopy(transferTemplate)

                xmlutils4.data_to_xml(transferOptions, transferNode, transferSpecObject, nsmap,
                                      # WARNING: doc_uuid isn't used here, but be careful
                                      None)
                transferMount.insert(transferIdx + i, transferNode)

            mount_node.insert(mount_index + distIdx, distributionNode)
            distIdx += 1
