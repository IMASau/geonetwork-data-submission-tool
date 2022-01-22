import copy
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
        assert len(nodes) == 1
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
    xpath = xf_props.get('xpath', None)
    assert data_path is not None, "export2_remove_element_handler: xf_props.data_path must be set"
    assert xpath is not None, "export2_remove_element_handler: xf_props.xpath must be set"
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

    assert len(mount_nodes) == 1
    assert len(template_nodes) == 1

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
                    xml_node=xml_node,
                    spec=items_spec,
                    xml_kwargs=xml_kwargs,
                    handlers=handlers)

        template.getparent().remove(template)


def export2_generateParameterKeywords_handler(data, xml_node, spec, xml_kwargs, handlers, xform):
    """
    Append keyword for each parameter.

    Configured with xf_props
    - xform[1].data_path
    - xform[1].mount_xpath
    - xform[1].template_xpath

    """
    xf_props = xform[1]
    data_path = xf_props.get('data_path', None)
    mount_xpath = xf_props.get('mount_xpath', None)
    template_xpath = xf_props.get('template_xpath', None)

    assert data_path is not None, "export2_generateParameterKeywords_handler: xf_props.data_path must be set"
    assert mount_xpath is not None, "export2_generateParameterKeywords_handler: xf_props.mount_xpath must be set"
    assert template_xpath is not None, "export2_generateParameterKeywords_handler: xf_props.template_xpath must be set"

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

        for i, value in enumerate(values):
            element = copy.deepcopy(template)
            # FIXME: Could make this generic like data_to_xml, but
            # that's a bit messy so just hard-code for now:
            anchor = element.xpath('gcx:Anchor', **xml_kwargs)[0]
            anchor.text = value['label']
            attrs = xmlutils4.parse_attributes(items_spec['uri'], xml_kwargs['namespaces'])
            for attr, f in attrs.items():
                anchor.set(attr, f(value['uri']))
            mount_node.insert(mount_index + i, element)


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

    assert data_path is not None, "export2_generateParameterKeywords_handler: xf_props.data_path must be set"
    assert mount_xpath is not None, "export2_generateParameterKeywords_handler: xf_props.mount_xpath must be set"
    assert template_xpath is not None, "export2_generateParameterKeywords_handler: xf_props.template_xpath must be set"

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
