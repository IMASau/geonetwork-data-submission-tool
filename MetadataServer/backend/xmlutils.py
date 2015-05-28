import datetime
from decimal import Decimal
import logging
import inspect
from copy import deepcopy

from django.utils.six import string_types

logger = logging.getLogger(__name__)

SPECIAL_KEYS = ['namespaces', 'nodes', 'xpath', 'export', 'attributes', 'container', 'parser', 'exportTo', 'keep',
                'default', 'removeWhen', 'template']


def get_value_type(eles):
    try:
        return eles[0].getchildren()[0].tag
    except:
        return None


def extract_fields(tree, spec, **kwargs):
    if isinstance(spec, list):
        spec = spec[0]
        many = True
    else:
        many = False

    if 'namespaces' in spec:
        kwargs['namespaces'] = spec['namespaces']

    eles = tree.xpath(spec['xpath'], **kwargs)

    if spec.get('required') or 'nodes' in spec:
        assert len(eles) > 0, "We require at least one xpath match for required fields and all branches.\n{0}\n{1}".format(spec['xpath'], eles)

    field = spec.copy()
    for special_key in SPECIAL_KEYS:
        if special_key in field: del field[special_key]

    if many:
        field['many'] = True
        field['initial'] = spec.get('initial', [])
        if 'nodes' in spec:
            field['fields'] = {k: extract_fields(eles[0], v, **kwargs)
                               for k, v in spec['nodes'].iteritems()}
        else:
            field['type'] = get_value_type(eles)

    elif 'nodes' in spec:
        for k, v in spec['nodes'].iteritems():
            field[k] = extract_fields(eles[0], v, **kwargs)

    else:
        field['type'] = get_value_type(eles)
        field['initial'] = spec.get('initial', None)

    return field


def value(ele, **kwargs):
    """
    Extract value.  Either tagged or text.  Always one value.

    :param ele:lxml.Element
    :return: a value
    """

    if ele is None:
        raise Exception("Expected a valid ele to extract value from")

    value_ele = ele.xpath('*', **kwargs)
    if value_ele and len(value_ele) == 1:
        value_ele = value_ele[0]
        value = value_ele.text
        tag = value_ele.tag

        if value is None:
            return None
        elif tag == "{http://www.isotc211.org/2005/gco}DateTime":
            return datetime.datetime.strptime(value, "%Y-%m-%dT%H:%M:%S")

        elif tag == "{http://www.isotc211.org/2005/gco}Date":
            return datetime.datetime.strptime(value, "%Y-%m-%d").date()

        elif tag == "{http://www.isotc211.org/2005/gco}Decimal":
            return Decimal(value)

        elif tag == "{http://www.isotc211.org/2005/gco}CharacterString":
            pass

        return value

    texts = ele.xpath("text()")
    if texts is None:
        pass
    elif len(texts) == 1:
        return texts[0]
    elif len(texts) > 1:
        assert "Didn't expect multiple results to text() xpath query: %s" % ele


def get_default(spec):
    default = spec.get('default', '')
    if hasattr(default, '__call__'):
        return default()
    else:
        return default


def process_node_child(ele, spec, **kwargs):
    if 'nodes' in spec:
        nodes = spec['nodes']
        if isinstance(nodes, dict):
            return {n: extract_xml_data(ele, s, **kwargs)
                    for n, s in spec['nodes'].iteritems()}
        else:
            assert "Expected dict.  Got %s" % type(nodes)
    else:
        if spec.get('keep', True) and spec.get('default', None) is None:
            if 'parser' in spec:
                return spec['parser'](ele)
            else:
                return value(ele, **kwargs)
        else:
            return get_default(spec)


def extract_xml_data(tree, spec, **kwargs):
    if isinstance(spec, list):
        spec = spec[0]
        many = True
    else:
        many = False

    required = spec.get('required', False)
    initial = spec.get('initial', None)

    if 'namespaces' in spec:
        kwargs['namespaces'] = spec['namespaces']

    eles = tree.xpath(spec['xpath'], **kwargs)

    if not isinstance(eles, list):
        if spec.get('keep', True) and spec.get('default', None) is None:
            return eles
        else:
            return get_default(spec)

    if not many:
        assert len(eles) < 2, \
            "XPath must resolve to single element:\n" \
            "ele: %s\n" \
            "node: %s\n" \
            "eles: %s" % (tree, spec, eles)

    if many:
        if spec.get('keep', True):
            return [process_node_child(ele, spec, **kwargs) for ele in eles]
        else:
            return []
    elif len(eles) == 0 and not required:
        return initial
    elif len(eles) == 1:
        return process_node_child(eles[0], spec, **kwargs)
    else:
        assert len(eles) > 0, ["No matches for required", spec["xpath"], tree]


identity = lambda x: x


def parse_attributes(spec):
    try:
        attrs = spec['attributes']
    except KeyError:
        return {'text': identity}
    if isinstance(attrs, list):
        return dict(zip(attrs, [identity] * len(attrs)))
    return attrs


def item_is_empty(data, k, v):
    return k not in data or data[k] is None or data[k] == '' or ('removeWhen' in v and v['removeWhen'](data[k]))


def spec_data_from_batch(batch_spec, key):
    assert isinstance(key, string_types), ("Expected a string key, but got {0}".format(type(key).__name__))
    data = {name: node['data'] for name, node in batch_spec[key].iteritems()}
    spec = {'xpath': '.', 'nodes': batch_spec[key]}
    return spec, data


def data_to_xml(data, parent, spec, nsmap, i=0, silent=True):
    if isinstance(spec, list):
        spec = spec[0]
        xpath = spec.get('container', spec['xpath'])
        container = parent.xpath(xpath, namespaces=nsmap)
        if spec.get('fanout', False):
            for i in range(len(container)):
                data_to_xml(data, parent, spec, nsmap, i, silent)
        else:
            if len(container) < 1:
                msg = "container at xpath %s is not found" % xpath
                print parent
                if silent:
                    logger.warning(msg)
                    return
                else:
                    raise Exception(msg)
            mount = container[0].getparent()
            template = deepcopy(container[0])
            for elem in container:
                mount.remove(elem)
            for i, item in enumerate(data):
                mount.append(deepcopy(template))
                data_to_xml(item, parent, spec, nsmap, i, silent)
    elif not spec.get('export', True):
        if 'exportTo' in spec:
            for v in spec['exportTo']:
                data_to_xml(data, parent, v, nsmap, i, silent)
    elif 'batch' in spec:
        spec, data = spec_data_from_batch(spec['batch'], data)
        data_to_xml(data, parent, spec, nsmap, 0, silent)
    elif 'nodes' in spec:
        parent = parent.xpath(spec['xpath'], namespaces=nsmap)[i]
        for k, v in spec['nodes'].iteritems():
            if item_is_empty(data, k, v):
                if isinstance(v, list):
                    v = v[0]
                if v.get('required', False):
                    # at the moment, we are always graceful to missing fields, only reporting them w/o raising exception
                    logger.warning('%s field is required, but missing' % k)
                xpath = v.get('container', v.get('xpath', None))
                if xpath is not None:
                    elems = parent.xpath(xpath, namespaces=nsmap)
                    for elem in elems:
                        elem.getparent().remove(elem)
                continue
            data_to_xml(data[k], parent, v, nsmap, 0, silent)
    else:
        elems = parent.xpath(spec['xpath'], namespaces=nsmap)
        if len(elems) < i + 1:
            msg = 'element %s[%d] not found in template, not written' % (spec['xpath'], i)
            if silent:
                logger.warning(msg)
            else:
                raise Exception(msg)
            return
        elem = elems[i]
        if len(elem.getchildren()) > 0:  # FIXME make explicit declaration in spec
            elem = elem.getchildren()[0]
        for attr, f in parse_attributes(spec).iteritems():
            arity = len(inspect.getargspec(f)[0])
            if arity == 1:
                v = f(data)
            elif arity == 2:
                source_value = value(elem, namespaces=nsmap)
                v = f(data, source_value)
            else:
                msg = 'attr %s in spec %s has unsupported arity %d' % (attr, str(spec), arity)
                if silent:
                    logger.warning(msg)
                    continue
                else:
                    raise Exception(msg)
            if attr == 'text':
                elem.text = v
            else:
                elem.set(attr, v)
        if 'exportTo' in spec:
            for v in spec['exportTo']:
                # export to list of nodes is usually about keeping their data, not cloning first node
                if isinstance(v, list):
                    v[0]['fanout'] = True
                data_to_xml(data, parent, v, nsmap, i, silent)
