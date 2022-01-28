
# Spec mapper documentation

*TODO: would be nice to have a sample input for reference as well,
plus an overview of how this is applied.  What I currently believe:
processing takes as input the mapping-spec (described here), the xml
template, and the json document input.  The xml template describes the
output, including boilerplate structure and default values.
Processing uses xpaths to find locations in the template to insert
values, and find nodes to further process / duplicate /etc.  It looks
like the json spec matches the json-doc data input in hierarchy; for
each key:subspec in the spec, the key is also used to index the data
document.*

*Nice-to-have: an overview of the input format and basic processing,
then summary of the special-cases required, necessitating
exportTo/container/etc*

## spec

Top level property for defining the spec mapping.

```json
{
  "spec": {
    "namespaces": {...},
    "xpath": "/mdb:MD_Metadata",
    "postprocess": ...,
    "nodes": {...}
  },
  "node_groups": {...}
}
```

## namespaces

Map of namespace mappings used in xpath evaluation in this spec.

```json
{
  "spec": {
    "namespaces": {
      "mdb": "http://standards.iso.org/iso/19115/-3/mdb/1.0",
      "xsi": "http://www.w3.org/2001/XMLSchema-instance",
      "cat": "http://standards.iso.org/iso/19115/-3/cat/1.0",
      "gfc": "http://standards.iso.org/iso/19110/gfc/1.1",
      "cit": "http://standards.iso.org/iso/19115/-3/cit/1.0",
      ...
    },
    "xpath": "/mdb:MD_Metadata",
    ...
  }
}
```

## postprocess

Takes a function name.  Allows for special behaviour at the end of export process.

```json
    "spec": {
        "namespaces": {...},
        "xpath": "/mdb:MD_Metadata",
        "postprocess": {
            "function": "prune_if_empty"
        },
```

```python

def prune_if_empty(data, parent, spec, nsmap, i, silent):
    """
    Catch-all processing to clean up specific nodes that may have been
    left with no content.

    """
    # descriptiveKeywords without any content; ie not empty, but don't have a gmd:keyword
    for elem in parent.findall('.//mri:descriptiveKeywords', nsmap):
        if elem.find('./mri:MD_Keywords/mri:keyword', nsmap) is None:
            elem.getparent().remove(elem)
    # No descendent text() at all:
    for xpath in ['mri:descriptiveKeywords',
                  'mri:resourceConstraints',
                  'mri:resourceMaintenance']:
        for elem in parent.findall('.//' + xpath, nsmap):
            if is_empty(elem):
                elem.getparent().remove(elem)
```


## nodes
Collection of XML nodes, no additional properties

```json
"attachments": {
  "many": true,
  "xpath": "mdb:distributionInfo/mrd:MD_Distribution/mrd:transferOptions/mrd:MD_DigitalTransferOptions/mrd:onLine[cit:CI_OnlineResource[cit:protocol/gco:CharacterString/text()=\"WWW:DOWNLOAD-1.0-http--download\"][cit:description/gco:CharacterString/text()=\"Data file\"]]",
  "keep": false,
  "nodes": {
    "file": {
      "xpath": "cit:CI_OnlineResource/cit:linkage/gco:CharacterString",
      "required": true,
      "attributes": {...}
    },
    "name": {...}
  }
},
```

## xpath

The `xpath` function to retrieve the XML fragment for processing the metadata property.
This can be a node or an attribute.

```json
"fileIdentifier": {
  "xpath": "mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code",
  "valueChild": "gco:CharacterString",
  "exportTo": {...}
},
```

## valueChild

The child node of the xpath that will be used to read/write.

This is slightly redundant when used stand-alone, but has a nice
synergy with `keep: false` for optional elements.  In the example
below, the value is nested in some structure (starting at `cit:date`), but if there is no value
we want to remove the entire structure:
```json
"datePublication": {
  "type": "string",
  "!docstring": "ID8",
  "xpath": "cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode[@codeListValue=\"publication\"]]",
  "valueChild": "cit:CI_Date/cit:date/gco:Date",
  "keep": false
}
```

To populate or remove (note how `xpath` selects the root, while
`valueChild` selects the location to insert the value under that root):
```xml
<cit:date>
  <cit:CI_Date>
    <cit:date>
      <gco:Date>2021-10-03</gco:Date>
    </cit:date>
    <cit:dateType>
      <cit:CI_DateTypeCode codeList="codeListLocation#CI_DateTypeCode" codeListValue="publication"/>
    </cit:dateType>
  </cit:CI_Date>
</cit:date>
```

## export

If `true` (default), the property in the metadata document will be exported in the final XML in the location
xpath/valueChild. 

If `false`, the property will not be include in the export.

*TODO: why would you not include it?  If used as a conditional for eg,
how is it accessed?*  (Update: see `exportTo`; is that the only case?)

```json
"hasGeographicCoverage": {
  "xpath": "gex:geographicElement[count(.)>0]",
  "export": false
},
```

## exportTo

Sometimes the location to write the property may be an unusual location, or further down the xpath than the point we
want to read from.

The `exportTo` definition allows the definition of an explicit write location, defined as an
`xpath` and `attributes` section.

If `export` is set to `true`, the export process will write to both the default location and the `exportTo` location.

The `exportTo` definition can also be a function, taking the enclosing
spec and data from that point in the schema, in which case it should
return a essentially a replacement for the current `spec`.  The
primary use-case for this is when the export location depends on the
data.

If `export` is `false` but `exportTo` is defined, then the property will be written only to the
`exportTo` location.

```json
"orcid": {
  "xpath": "cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:name/gcx:Anchor[@xlink:role=\"orcid\"]",
  "required": false,
  "keep": false,
  "export": false,
  "parser": {
    "function": "parse_individual_orcid"
  },
  "exportTo": {
    "xpath": "cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:name/gcx:Anchor",
    "attributes": {
      "xlink:href": {
        "function": "write_orcid"
      },
      "xlink:role": {
        "function": "write_orcid_role"
      }
    }
  }
},
```

```python
def parse_individual_orcid(x):
    orcid_uri = x.attrib['{http://www.w3.org/1999/xlink}href']
    match = re.search(r"0000-000(1-[5-9]|2-[0-9]|3-[0-4])\d\d\d-\d\d\d[\dX]", orcid_uri)
    orcid = match.group(0)
    return orcid

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

SPEC_FUNCTIONS = {
    "parse_individual_orcid": parse_individual_orcid,
    "write_orcid": write_orcid,
    "write_orcid_role": write_orcid_role,
}
```

An example of programatic mapping:

```json
"SpatialResolution": {
  "type": "object",
  "xpath": "mri:spatialResolution/mri:MD_Resolution",
  "properties": {
    "ResolutionAttribute": {
      "!docstring": "Where9",
      "type": "string"
    },
    "ResolutionAttributeValue": {
      "!docstring": "Where10",
      "type": "number"
    },
    "ResolutionAttributeUnits": {
      "!docstring": "Derived; depends on the value of Where9",
      "type": "string"
    }
  },
  "export": false,
  "exportTo": {
    "function": "spatial_units_export"
  },
}
```

```python
def spatial_units_export(data):
    # Based on the data passed in, map to the appropriate xml element.
    # Note that at this point the unit attribute is hard-coded so we
    # are only concerned with inserting the value:
    attr = data.get('ResolutionAttribute')
    unitToXPath = {
        'Denominator scale': 'mri:equivalentScale/mri:MD_RepresentativeFraction/mri:denominator/gco:Integer',
        'Vertical': 'mri:vertical/gco:Distance',
        'Horizontal': 'mri:distance/gco:Distance',
        'Angular distance': 'mri:angularDistance/gco:Angle',
    }
    if attr and attr in unitToXPath:
        subElementPath = unitToXPath[attr]
        return {
            "type": "object",
            "xpath": f"mri:spatialResolution/mri:MD_Resolution/{subElementPath}",
            "properties": {
                "ResolutionAttributeValue": {
                    "xpath": ".",
                    'attributes': {
                        'text': to_string
                    }
                },
            }
        }
    else:
        # Nothing to map, just return an empty spec:
        return {}
```

## append

If `append` is `true` (default is `false`), arrays will be exported by
ignoring the `element_index` parameter and using the last matching
element as the template.

In certain situations, regular xpaths aren't expressive enough for
standard `array` exports. The canonical case is where the only
distinguishing feature of a template element is its position, for
example we have several nodes of fixed content (eg, "Other
Constraints"), then the final node should be our template. This
doesn't work because recursive processing will identify a single node
every time, regardless of how many elements have been exported.

Example, noting the `[last()]` clause:
```json
"additionalConstraints": {
  "!docstring": "About1",
  "keep": false,
  "type": "object",
  "xpath": "mri:resourceConstraints/mco:MD_LegalConstraints",
  "properties": {
    "constraints": {
      "keep": false,
      "type": "array",
      "xpath": "mco:otherConstraints",
      "container": "mco:otherConstraints[last()]",
      "items": {
        "type": "string",
        "keep": false,
        "append": true,
        "xpath": "mco:otherConstraints",
        "valueChild": "gco:CharacterString"
      }
    }
  }
},

```

## keep

If `keep` is set to `true` (default), the value in the XML template file will be used as the default value for any new
document created.

If `keep` is set to `false`, the value will be initiated as null.

```json
"positionName": {
  "xpath": "cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:positionName",
  "keep": false,
  "valueChild": "gco:CharacterString",
  "required": false
},
```

## default

If `default` is set, this value will be used as the default value for any new document created. This can either be a
simple value (not intended for arrays or objects) or a function (see `functions`, below).

Note: a default value defined here will override whatever is set in the XML template (i.e. `default` takes precedence over
`{"keep": true, ...}`)

```json
"dateCreation": {
  "xpath": "mri:citation/cit:CI_Citation/cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode[@codeListValue=\"creation\"]]/cit:date",
  "valueChild": "gco:DateTime",
  "required": true,
  "default": {
    "function": "today"
  }
},
```

## container

This is an xpath that optionally defines the container XML fragment
for the property.

*TODO: why this, rather than hierarchy (ie, why is the container not
just the parent of this node?)*

```json
"keywords": {
  "xpath": "mri:keyword/gco:CharacterString",
  "many": true,
  "container": "mri:keyword",
  "keep": false,
  "export": false,
  "exportTo": {
    "xpath": "mri:keyword/gco:CharacterString"
  }
}
```


## Attributes
Optionally allows the definition of which attributes on the destination node to write to. 

If any `attributes` are defined, you must also define `text` if you want to write to the value of an XML node. 

*TODO: that makes sense, but why text in this example?  I read it as
there also being a text child, but the particular example doesn't seem
to have one*

If no `attributes` are defined, this is the default write location.

```json
"status": {
  "xpath": "mri:status",
  "valueChild": "mcc:MD_ProgressCode",
  "attributes": {
    "text": {
      "function": "identity"
    },
    "codeListValue": {
      "function": "identity"
    }
  },
  ...
},
```

## function

The function to call when processing this node's value.   

A full list of functions can be found in `backend.spec.SPEC_FUNCTIONS`. 

NOTE: arity of functions is considered.  multi arity ones take args parameter.  Could be normalised.

NOTE: the `identity` function returns the value as passed in. This is useful when you want to write to the text value 
and, for example, the codeListValue attribute, but don’t need to modify the value, this can be with.

```json
{
  "spec": {
    "namespaces": {
      ...
    },
    "xpath": "/mdb:MD_Metadata",
    "postprocess": {
      "function": "prune_if_empty"
    },
    "nodes": {
      "fileIdentifier": {
        "xpath": "mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code",
        "valueChild": "gco:CharacterString",
        "exportTo": {
          "xpath": "mdb:metadataLinkage/cit:CI_OnlineResource/cit:linkage/gco:CharacterString",
          "attributes": {
            "text": {
              "function": "geonetwork_url"
            }
          }
        }
      },
      ...
    }
  }
}
```

```python
def geonetwork_url(x):
    return 'https://geonetwork.tern.org.au/geonetwork/srv/eng/catalog.search#/metadata/{uuid}'.format(uuid=x)

SPEC_FUNCTIONS = {
    ...
    "geonetwork_url": geonetwork_url,
    ...
}
```

## required 

Indicates field is required.  Also used to check if all data is present before validating.

If `required` is set to `false` (default) indicates that a value is not required.

If `required` is set to `true` indicates that it is required.

```json
"dataSources": {
  "many": true,
  "xpath": "mdb:distributionInfo/mrd:MD_Distribution/mrd:transferOptions/mrd:MD_DigitalTransferOptions/mrd:onLine[cit:CI_OnlineResource/cit:protocol/gco:CharacterString/text()!=\"WWW:LINK-1.0-http--link\" and not(cit:CI_OnlineResource/cit:protocol/gco:CharacterString/text()=\"WWW:DOWNLOAD-1.0-http--download\" and cit:CI_OnlineResource/cit:description/gco:CharacterString/text()=\"Data file\")]",
  "keep": false,
  "nodes": {
    "description": {
      "label": "Description",
      "xpath": "cit:CI_OnlineResource/cit:description",
      "valueChild": "gco:CharacterString",
      "required": true
    },
    "url": {
      "xpath": "cit:CI_OnlineResource/cit:linkage",
      "valueChild": "gco:CharacterString",
      "required": true
    },
    "name": {
      "xpath": "cit:CI_OnlineResource/cit:name",
      "valueChild": "gco:CharacterString",
      "required": false
    },
    "protocol": {
      "xpath": "cit:CI_OnlineResource/cit:protocol",
      "valueChild": "gco:CharacterString",
      "required": true
    }
  }
},
```


## xpath_required 

Indicates that the xpath expression must match something for the XML Template to be valid.

Useful for ensuring a xml elements are present for reuse generating records in a lst.

If `xpath_required` is set to `false` (default) indicates that a xpath expression doesn't need to find matching elements.

If `xpath_required` is set to `true` indicates that xpath expression must find at least one result.

```json
"dataSources": {
  "many": true,
  "xpath": "mdb:distributionInfo/mrd:MD_Distribution/mrd:transferOptions/mrd:MD_DigitalTransferOptions/mrd:onLine[cit:CI_OnlineResource/cit:protocol/gco:CharacterString/text()!=\"WWW:LINK-1.0-http--link\" and not(cit:CI_OnlineResource/cit:protocol/gco:CharacterString/text()=\"WWW:DOWNLOAD-1.0-http--download\" and cit:CI_OnlineResource/cit:description/gco:CharacterString/text()=\"Data file\")]",
  "keep": false,
  "nodes": {
    "description": {
      "label": "Description",
      "xpath": "cit:CI_OnlineResource/cit:description",
      "valueChild": "gco:CharacterString",
      "xpath_required": true
    }
  }
},
```


## !docstring

Ignored by processing, used for comments.

```json
  "isUserAdded": {
    "!docstring": "Fake node so we can track person status",
    "xpath": "isUserAdded",
    "default": false,
    "export": false
  }
```

## deleteWhenEmpty

Some things in the ISO standard can't be empty (when present).

If `deleteWhenEmpty` is set to `true` (default), the xpath node should not be included in the final XML unless the value is set. 

If `deleteWhenEmpty` is set to `false`, the node should be included even if no value is set

```json
  "uri": {
    "xpath": "cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:name/gcx:Anchor[@xlink:role=\"uri\"]",
    "keep": false,
    "required": false,
    "deleteWhenEmpty": false,
    "export": false,
    ...
  },
```

## removeWhen

Optionally define a function to specify when the node should be deleted from the exported XML.

```json
"geographicElement": {
  "xpath": "mri:extent/gex:EX_Extent[gex:geographicElement][gex:temporalElement]",
  "removeWhen": {
    "function": "no_geographic_coverage"
  },
  "nodes": {...}
}
```

```python
def no_geographic_coverage(x):
    return not x.get('hasGeographicCoverage', True)

SPEC_FUNCTIONS = {
    "no_geographic_coverage": no_geographic_coverage
}
```

## node_groups

Top level property.  Lets you define a set of nodes that can be re-used throughout the mapper, rather than defining the same set repeatedly.

For example, citedResponsibleParty and pointOfContact both have their node attributes set to the 
node group “ci_responsible_party_nodes” which is then defined in the node_groups section

```json
{
  "spec": {...},
  "node_groups": {
    "ci_responsible_party_nodes": {
      ...
    }
  }
  "pointOfContact": {
    "many": true,
    "xpath": "mri:pointOfContact/cit:CI_Responsibility",
    "container": "mri:pointOfContact",
    "nodes": "ci_responsible_party_nodes"
  }
}
```

## Examples (add-to and refine this section over time)

We avoid the need for `exportTo` here.  Note that `keywords` is an
`array`; the `xpath` prop here is exactly the same as `container` (in
fact, `get_container` looks for a `container` prop and then falls back
to `xpath`).  This container is cloned and used as the template for
the `items`.  The spec in the `items` entry is iterated over each
element in the corresponding data array.  The `items.xpath` is
searched from the *container's parent*, indexed by the position in the
data (ie, in this example because it also matches `items.xpath`, the
most-recently cloned and mounted container).  No mapping functions are
needed because the values are implicit in the object's property data
values.

```json
"keywordsTheme": {
  "xpath": "mri:descriptiveKeywords/mri:MD_Keywords[not(@uuid=\"Data_Group\")][mri:type/mri:MD_KeywordTypeCode[@codeListValue=\"theme\"]][mri:thesaurusName/cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString/text()=\"https://gcmd.earthdata.nasa.gov/kms/concepts/concept_scheme/sciencekeywords\"]",
  "type": "object",
  "properties": {
    "keywords": {
      "xpath": "mri:keyword",
      "keep": false,
      "type": "array",
      "items": {
        "type": "object",
        "xpath": "mri:keyword",
        "properties": {
          "label": {
            "xpath": "gcx:Anchor"
          },
          "uri": {
            "xpath": "gcx:Anchor",
            "attributes": {
              "xlink:href": {
                "function": "identity"
              }
            }
          }
        }
      }
    }
  },
  "required": true
}
```
