import datetime

import re

NIL_ATTR = '{http://www.isotc211.org/2005/gco}nilReason'

CI_RESPONSIBLE_PARTY_NODES = {
    'individualName': {
        'xpath': 'gmd:individualName',
        'required': True,
    },
    'orcid': {
        'xpath': 'gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL',
        'container': 'gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource',
    },
    'organisationName': {
        'xpath': 'gmd:organisationName',
        'required': True,
    },
    'role': {
        'xpath': 'gmd:role',
        'attributes': ['text', 'codeListValue'],
        'required': True
    },
    'address': {
        'xpath': 'gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address',
        'nodes': {
            'deliveryPoint': {'xpath': 'gmd:deliveryPoint[1]'},
            'deliveryPoint2': {'xpath': 'gmd:deliveryPoint[2]'},
            'city': {'xpath': 'gmd:city'},
            'administrativeArea': {'xpath': 'gmd:administrativeArea'},
            'postalCode': {'xpath': 'gmd:postalCode'},
            'country': {'xpath': 'gmd:country'},
        }
    },
    'electronicMailAddress': {
        'xpath': 'gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress',
        'required': True,
    },
    'phone': {
        'xpath': 'gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice',
        'required': False
    },
    'facsimile': {
        'xpath': 'gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile',
        'required': False
    },
}


# Lookup of spec fragments, keyed by the creativeCommons data key,
# that will be inserted into the spec at that same point and
# recursively processed.
LICENCE_SPEC = {
    'http://creativecommons.org/licenses/by/2.5/au/': {
        'licenseLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseLink',
            'data': 'http://creativecommons.org/licenses/by/2.5/au/'
        },
        'jurisdictionLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:jurisdictionLink',
            'data': 'http://creativecommons.org/international/au/'
        },
        'imageLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:imageLink',
            'data': 'http://i.creativecommons.org/l/by/2.5/au/88x31.png'
        },
        'licenseName': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseName',
            'data': 'Attribution 2.5 Australia'
        },
    },
    'http://creativecommons.org/licenses/by-nc/2.5/au/': {
        'licenseLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseLink',
            'data': 'http://creativecommons.org/licenses/by-nc/2.5/au/'
        },
        'jurisdictionLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:jurisdictionLink',
            'data': 'http://creativecommons.org/international/au/'
        },
        'imageLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:imageLink',
            'data': 'http://i.creativecommons.org/l/by-nc/2.5/au/88x31.png'
        },
        'licenseName': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseName',
            'data': 'Attribution-NonCommercial 2.5 Australia'
        },
    },
    'http://creativecommons.org/licenses/by/3.0/au/': {
        'licenseLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseLink',
            'data': 'http://creativecommons.org/licenses/by/3.0/au/'
        },
        'jurisdictionLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:jurisdictionLink',
            'data': 'http://creativecommons.org/international/au/'
        },
        'imageLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:imageLink',
            'data': 'http://i.creativecommons.org/l/by/3.0/au/88x31.png'
        },
        'licenseName': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseName',
            'data': 'Attribution 3.0 Australia'
        },
    },
    'http://creativecommons.org/licenses/by-nc/3.0/au/': {
        'licenseLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseLink',
            'data': 'http://creativecommons.org/licenses/by-nc/3.0/au/'
        },
        'jurisdictionLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:jurisdictionLink',
            'data': 'http://creativecommons.org/international/au/'
        },
        'imageLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:imageLink',
            'data': 'http://i.creativecommons.org/l/by-nc/3.0/au/88x31.png'
        },
        'licenseName': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseName',
            'data': 'Attribution-NonCommercial 3.0 Australia'
        },
    },
    # FIXME fake link to identify "Other" licensing, should be corrected by Data Manager anyway after export
    # as noted by customer:
    # We'd like to encourage people to use the first or second option,
    # but if they choose "other constraints" that will allow the Data Manger to follow up with the user
    # to find out what other constraints they require (rather than clogging up the tool with a heap of options
    # that most people won't understand anyway).
    'http://creativecommons.org/licenses/other': {
        'licenseLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseLink',
            'data': 'http://creativecommons.org/licenses/other'
        },
        'jurisdictionLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:jurisdictionLink',
            'data': 'http://creativecommons.org/international/au/'
        },
        'imageLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:imageLink',
            'data': 'http://i.creativecommons.org/l/by/2.5/au/88x31.png'
        },
        'licenseName': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseName',
            'data': 'Attribution 2.5 Australia'
        },
    },
}

LINKAGE_UUID = re.compile(r'uuid=\w{8}-\w{4}-\w{4}-\w{4}-\w{12}')


def make_spec(**kwargs):
    return {
        'namespaces': {
            'mcp': 'http://schemas.aodn.org.au/mcp-2.0',
            'gmd': 'http://www.isotc211.org/2005/gmd',
            'gmx': 'http://www.isotc211.org/2005/gmx',
            'xlink': 'http://www.w3.org/1999/xlink',
            'dwc': 'http://rs.tdwg.org/dwc/terms/',
            'gml': 'http://www.opengis.net/gml',
            'gco': 'http://www.isotc211.org/2005/gco',
            'xsi': 'http://www.w3.org/2001/XMLSchema-instance',
            'geonet': 'http://www.fao.org/geonetwork',
        },
        'xpath': '/mcp:MD_Metadata',
        'nodes': {
            'fileIdentifier': {
                'xpath': 'gmd:fileIdentifier',
                'keep': False,
                'exportTo': [[{'xpath': 'gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/'
                                        'gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/'
                                        'gmd:linkage/gmd:URL',
                               'attributes': {'text': lambda x, y: re.sub(LINKAGE_UUID, "uuid=" + x, y)}}]],
            },
            'dateStamp': {
                'xpath': 'gmd:dateStamp',
            },
            'identificationInfo': {
                'xpath': 'gmd:identificationInfo/mcp:MD_DataIdentification',
                'nodes': {
                    'title': {
                        'xpath': 'gmd:citation/gmd:CI_Citation/gmd:title',
                        'label': 'Title',
                        'required': True
                    },
                    'dateCreation': {
                        'xpath': 'gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode'
                                 '[@codeListValue="creation"]]/gmd:date',
                        'required': True,
                        'default': lambda: datetime.date.today().strftime('%Y-%m-%d')
                    },
                    'topicCategory': {
                        'xpath': 'gmd:topicCategory',
                        'required': True
                    },
                    'status': {
                        'xpath': 'gmd:status',
                        'attributes': ['text', 'codeListValue'],
                        'notes': 'Status of data',
                        'required': True
                    },
                    'maintenanceAndUpdateFrequency': {
                        'xpath': 'gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency',
                        'attributes': ['text', 'codeListValue'],
                        # 'required': True,  # FIXME depends on status
                    },
                    'samplingFrequency': {
                        'xpath': 'mcp:samplingFrequency',
                        'label': 'Sampling Frequency',
                        'attributes': ['text', 'codeListValue'],
                    },
                    'abstract': {
                        'xpath': 'gmd:abstract',
                        'label': 'Abstract',
                        'required': True,
                    },
                    'keywordsTheme': {
                        'xpath': 'gmd:descriptiveKeywords/gmd:MD_Keywords'
                                 '[gmd:type/gmd:MD_KeywordTypeCode/text()="theme"]'
                                 '[gmd:thesaurusName]',
                        'nodes': {
                            'keywords': [{'container': 'gmd:keyword',
                                          'xpath': 'gmd:keyword/gmx:Anchor',
                                          'keep': False,
                                          'export': False,
                                          'parser': lambda x: x.get('{http://www.w3.org/1999/xlink}href').split('/')[
                                              -1],
                                          'exportTo': [{'xpath': 'gmd:keyword/gmx:Anchor',
                                                        'attributes':
                                                            {'{http://www.w3.org/1999/xlink}href':
                                                                 lambda x:
                                                                 'http://metadata.imas.utas.edu.au:/geonetwork/srv/eng/'
                                                                 'xml.keyword.get?thesaurus=external.theme.sciencekeywords'
                                                                 '&id=http://gcmdservices.gsfc.nasa.gov/kms/concept/'
                                                                 + x,
                                                             'text':
                                                                 lambda x: kwargs['science_keyword'].objects.get(
                                                                     UUID=x).as_str()}}]}]
                        },
                        'required': True,
                        'notes': 'Theme keywords (selecting from controlled list in thesaurus)'
                    },
                    'keywordsThemeExtra': {
                        'xpath': 'gmd:descriptiveKeywords/gmd:MD_Keywords'
                                 '[gmd:type/gmd:MD_KeywordTypeCode/text()="theme"]'
                                 '[not(gmd:thesaurusName)]',
                        'nodes': {
                            'keywords': [{'xpath': 'gmd:keyword', 'keep': False}]
                        },
                        'notes': 'Theme keywords (free text entry)'
                    },
                    'keywordsTaxonExtra': {
                        'xpath': 'gmd:descriptiveKeywords/gmd:MD_Keywords'
                                 '[gmd:type/gmd:MD_KeywordTypeCode/text()="taxon"]',
                        'nodes': {
                            'keywords': [{'xpath': 'gmd:keyword', 'keep': False}]
                        },
                        'notes': 'Taxon keywords (free text entry)'
                    },
                    'beginPosition': {
                        'xpath': 'gmd:extent/gmd:EX_Extent'
                                 '/gmd:temporalElement/mcp:EX_TemporalExtent'
                                 '/gmd:extent/gml:TimePeriod/gml:beginPosition',
                        'required': True,
                        'attributes': {
                            'text': lambda x: datetime.datetime.strptime(x[:10], "%Y-%m-%d").date().isoformat()}
                    },
                    'endPosition': {
                        'xpath': 'gmd:extent/gmd:EX_Extent'
                                 '/gmd:temporalElement/mcp:EX_TemporalExtent'
                                 '/gmd:extent/gml:TimePeriod/gml:endPosition',
                        # 'required': True,  # FIXME depends on status
                        'attributes': {
                            'text': lambda x: datetime.datetime.strptime(x[:10], "%Y-%m-%d").date().isoformat()}
                    },
                    'geographicElement': [{'xpath': 'gmd:extent/gmd:EX_Extent/gmd:geographicElement/'
                                                    'gmd:EX_GeographicBoundingBox',
                                           'required': True,
                                           'keep': False,
                                           'nodes': {
                                               'westBoundLongitude': {
                                                   'xpath': 'gmd:westBoundLongitude',
                                                   'required': True,
                                                   'attributes': {'text': lambda x: str(x)},
                                               },
                                               'eastBoundLongitude': {
                                                   'xpath': 'gmd:eastBoundLongitude',
                                                   'required': True,
                                                   'attributes': {'text': lambda x: str(x)},
                                               },
                                               'southBoundLatitude': {
                                                   'xpath': 'gmd:southBoundLatitude',
                                                   'required': True,
                                                   'attributes': {'text': lambda x: str(x)},
                                               },
                                               'northBoundLatitude': {
                                                   'xpath': 'gmd:northBoundLatitude',
                                                   'required': True,
                                                   'attributes': {'text': lambda x: str(x)},
                                               },
                                           }}],
                    'verticalElement': {
                        'xpath': 'gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent',
                        'removeWhen': lambda x: not x.get('hasVerticalExtent', False),
                        'nodes': {
                            'hasVerticalExtent': {
                                'xpath': 'count(.)>0',
                                'export': False,
                                'default': False,
                            },
                            'minimumValue': {
                                'xpath': 'gmd:minimumValue',
                                'required': True,
                            },
                            'maximumValue': {
                                'xpath': 'gmd:maximumValue',
                                'required': True,
                            },
                            'verticalCRS': {
                                'xpath': 'gmd:verticalCRS/gmx:ML_VerticalCRS/gml:identifier',
                                'required': True,
                                'exportTo': [
                                    {'xpath': 'gmd:verticalCRS/gmx:ML_VerticalCRS/gml:name',
                                     'attributes': {'text': lambda x: {'EPSG::5715': 'MSL depth',
                                                                       'EPSG::5714': 'MSL height'}.get(x)}},
                                ]
                            }
                            # TODO: dist below sea level, dist above sea floor, above mean sea level...
                        }
                    },
                    'citedResponsibleParty': [
                        {
                            'xpath': 'gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty',
                            'container': 'gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty',
                            'nodes': CI_RESPONSIBLE_PARTY_NODES,
                            'notes': 'Responsible party/ies for creating dataset'
                        }
                    ],
                    'credit': [{'xpath': 'gmd:credit',
                                'notes': 'Other credits',
                                'keep': False}],
                    'pointOfContact': [
                        {
                            'xpath': 'gmd:pointOfContact/gmd:CI_ResponsibleParty',
                            'container': 'gmd:pointOfContact',
                            'nodes': CI_RESPONSIBLE_PARTY_NODES
                        }
                    ],
                    # TODO: This is an identifier but there's more in the creative commons chunk which needs updating
                    'creativeCommons': {
                        'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:licenseLink',
                        'batch': LICENCE_SPEC,
                    },
                    'otherConstraints': {
                        'xpath': 'gmd:resourceConstraints/mcp:MD_Commons/mcp:otherConstraints',
                        'is-hidden': True,
                    },
                    # TODO: Currently not used
                    # 'securityConstraints': {
                    # 'xpath': 'gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification'
                    # },
                    # TODO: this is another 'include chunk if true' type template inclusion
                    'useLimitation': {
                        'xpath': 'gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation',
                    },
                    'supplementalInformation': [
                        {
                            'xpath': 'gmd:supplementalInformation',
                            'keep': False
                        }
                    ],
                    'dataParameters': [
                        {
                            'xpath': 'mcp:dataParameters/mcp:DP_DataParameters/mcp:dataParameter/mcp:DP_DataParameter',
                            'container': 'mcp:dataParameters/mcp:DP_DataParameters/mcp:dataParameter',
                            'keep': False,
                            'nodes': {
                                'name': {
                                    'xpath': 'mcp:parameterName/mcp:DP_Term'
                                             '[mcp:type/mcp:DP_TypeCode/@codeListValue="shortName"]'
                                             '/mcp:term',
                                    'required': True,
                                },
                                'longName': {
                                    'xpath': 'mcp:parameterName/mcp:DP_Term'
                                             '[mcp:type/mcp:DP_TypeCode/@codeListValue="longName"]'
                                             '/mcp:term',
                                    'required': False,
                                },
                                'unit': {
                                    'xpath': 'mcp:parameterUnits/mcp:DP_Term'
                                             '[mcp:type/mcp:DP_TypeCode/@codeListValue="longName"]'
                                             '/mcp:term',
                                    'required': True,
                                },
                                'parameterMinimumValue': {
                                    'xpath': 'mcp:parameterMinimumValue'
                                },
                                'parameterMaximumValue': {
                                    'xpath': 'mcp:parameterMaximumValue'
                                },
                                'parameterDescription': {
                                    'xpath': 'mcp:parameterDescription'
                                },
                            }
                        }
                    ]
                }
            },
            'distributionInfo': {
                'xpath': 'gmd:distributionInfo/gmd:MD_Distribution',
                'nodes': {
                    'distributionFormat': {
                        'xpath': 'gmd:distributionFormat/gmd:MD_Format',
                        'nodes': {
                            'name': {'xpath': 'gmd:name'},
                            'version': {'xpath': 'gmd:version'},
                        }
                    },
                }
            },
            'dataQualityInfo': {
                'xpath': 'gmd:dataQualityInfo',
                'nodes': {
                    'statement': {
                        'xpath': 'gmd:DQ_DataQuality'
                                 '[gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode[@codeListValue="dataset"]]'
                                 '/gmd:lineage/gmd:LI_Lineage/gmd:statement',
                        'required': True,
                    }
                }
            }
        }
    }
