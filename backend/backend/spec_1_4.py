import datetime

from backend.models import ScienceKeyword

NIL_ATTR = '{http://www.isotc211.org/2005/gco}nilReason'

CI_RESPONSIBLE_PARTY_NODES = {
    'individualName': {
        'xpath': 'gmd:individualName',
        'required': True,
    },
    'organisationName': {
        'xpath': 'gmd:organisationName',
        'required': True,
    },
    'positionName': {
        'xpath': 'gmd:positionName',
        'required': False
    },
    'role': {
        'xpath': 'gmd:role',
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
            'xpath': 'gmd:resourceConstraints/mcp:MD_CreativeCommons/mcp:licenseLink',
            'data': 'http://creativecommons.org/licenses/by/2.5/au/'
        },
        'jurisdictionLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_CreativeCommons/mcp:jurisdictionLink',
            'data': 'http://creativecommons.org/international/au/'
        },
        'imageLink': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_CreativeCommons/mcp:imageLink',
            'data': 'http://i.creativecommons.org/l/by/2.5/au/88x31.png'
        },
        'licenseName': {
            'xpath': 'gmd:resourceConstraints/mcp:MD_CreativeCommons/mcp:licenseName',
            'data': 'Attribution 2.5 Australia'
        },
    },
}

spec = {
    'namespaces': {
        'mcp': 'http://bluenet3.antcrc.utas.edu.au/mcp',
        'xsi': 'http://www.w3.org/2001/XMLSchema-instance',
        'gml': 'http://www.opengis.net/gml',
        'gts': 'http://www.isotc211.org/2005/gts',
        'gco': 'http://www.isotc211.org/2005/gco',
        'gmd': 'http://www.isotc211.org/2005/gmd',
        'geonet': 'http://www.fao.org/geonetwork',
        'gmx': "http://www.isotc211.org/2005/gmx",
        'xlink': "http://www.w3.org/1999/xlink",
    },
    'xpath': '/mcp:MD_Metadata',
    'nodes': {
        'fileIdentifier': {
            'xpath': 'gmd:fileIdentifier'
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
                    'required': True,
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
                             '[gmd:thesaurusName/gmd:CI_Citation/gmd:title/*[contains(text(), "Science Keywords")]]',
                    'nodes': {
                        'keywords': [{'container': 'gmd:keyword',
                                      'xpath': 'gmd:keyword/gmx:Anchor',
                                      'export': False,
                                      'parser': lambda x: x.get('{http://www.w3.org/1999/xlink}href').split('/')[-1],
                                      'exportTo': [{'xpath': 'gmd:keyword/gmx:Anchor',
                                                    'attributes': {'{http://www.w3.org/1999/xlink}href': lambda
                                                        x: 'http://localhost:8080/geonetwork/srv/eng/xml.keyword.get?thesaurus=external.temporal.sciencekeywords&amp;id=http://gcmdservices.gsfc.nasa.gov/kms/concept/' + x,
                                                                   'text': lambda x: ScienceKeyword.objects.get(
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
                        'keywords': [{'xpath': 'gmd:keyword'}]
                    },
                    'notes': 'Theme keywords (free text entry)'
                },
                'keywordsTaxonExtra': {
                    'xpath': 'gmd:descriptiveKeywords/gmd:MD_Keywords'
                             '[gmd:type/gmd:MD_KeywordTypeCode/text()="taxon"]',
                    'nodes': {
                        'keywords': [{'xpath': 'gmd:keyword'}]
                    },
                    'notes': 'Taxon keywords (free text entry)'
                },
                'beginPosition': {
                    'xpath': 'gmd:extent/gmd:EX_Extent'
                             '/gmd:temporalElement/mcp:EX_TemporalExtent'
                             '/gmd:extent/gml:TimePeriod/gml:beginPosition',
                    'required': True,
                    'attributes': {'text': lambda x: datetime.datetime.strptime(x[:10], "%Y-%m-%d").date().isoformat()}
                },
                'endPosition': {
                    'xpath': 'gmd:extent/gmd:EX_Extent'
                             '/gmd:temporalElement/mcp:EX_TemporalExtent'
                             '/gmd:extent/gml:TimePeriod/gml:endPosition',
                    # 'required': True,  # FIXME depends on status
                    'attributes': {'text': lambda x: datetime.datetime.strptime(x[:10], "%Y-%m-%d").date().isoformat()}
                },
                'geographicElement': [{
                    'xpath': 'gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox',
                    'required': True,
                    'nodes': {
                        'westBoundLongitude': {
                            'xpath': 'gmd:westBoundLongitude',
                            'required': True,
                            'attributes': {'text': str},
                        },
                        'eastBoundLongitude': {
                            'xpath': 'gmd:eastBoundLongitude',
                            'required': True,
                            'attributes': {'text': str},
                        },
                        'southBoundLatitude': {
                            'xpath': 'gmd:southBoundLatitude',
                            'required': True,
                            'attributes': {'text': str},
                        },
                        'northBoundLatitude': {
                            'xpath': 'gmd:northBoundLatitude',
                            'required': True,
                            'attributes': {'text': str},
                        },
                    }
                }],
                'verticalElement': {
                    'xpath': 'gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent',
                    'removeWhen': lambda x: not x.get('hasVerticalExtent', False),
                    'nodes': {
                        'hasVerticalExtent': {
                            'xpath': 'count(.)>0',
                            'export': False
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
                            'xpath': 'gmd:verticalCRS/gml:VerticalCRS/gml:identifier',
                            'required': True,
                            'exportTo': [
                                {'xpath': 'gmd:verticalCRS/gml:VerticalCRS/gml:name',
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
                'credit': [{
                    'xpath': 'gmd:credit',
                    'notes': 'Other credits'
                }],
                'pointOfContact': {
                    'xpath': 'gmd:pointOfContact/gmd:CI_ResponsibleParty',
                    'nodes': CI_RESPONSIBLE_PARTY_NODES
                },
                # TODO: This is an identifier but there's more in the creative commons chunk which needs updating
                'creativeCommons': {
                    'xpath': 'gmd:resourceConstraints/mcp:MD_CreativeCommons/mcp:licenseLink',
                    'batch': LICENCE_SPEC,
                    'required': True,
                    'keep': True
                },
                # TODO: Currently not used
                # 'securityConstraints': {
                #     'xpath': 'gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification'
                # },
                # TODO: this is another 'include chunk if true' type template inclusion
                'useLimitation': {
                    'xpath': 'gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation',
                    'keep': True
                },
                'supplementalInformation': [{
                    'xpath': 'gmd:supplementalInformation'
                }],
                'dataParameters': [
                    {
                        'xpath': 'mcp:dataParameters/mcp:DP_DataParameters/mcp:dataParameter/mcp:DP_DataParameter',
                        'container': 'mcp:dataParameters/mcp:DP_DataParameters/mcp:dataParameter',
                        'nodes': {
                            'name': {
                                'xpath': 'mcp:parameterName/mcp:DP_ParameterName'
                                # '[mcp:type/mcp:DP_TypeCode/@codeListValue="shortName"]'  # FIXME iso19139b.xml hasn't this wrapper
                                         '/mcp:name',
                                'required': True,
                            },
                            'longName': {
                                'xpath': 'mcp:parameterName/mcp:DP_ParameterName'
                                         '[mcp:type/mcp:DP_TypeCode/@codeListValue="longName"]'
                                         '/mcp:name',
                                'required': False,
                            },
                            'unit': {
                                'xpath': 'mcp:parameterUnits/mcp:DP_UnitsName'
                                         '[mcp:type/mcp:DP_TypeCode/@codeListValue="longName"]'
                                         '/mcp:name',
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
                'distributorContact': [{
                    'xpath': 'gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty',
                    'container': 'gmd:distributor/gmd:MD_Distributor/gmd:distributorContact',
                    'nodes': CI_RESPONSIBLE_PARTY_NODES
                }],
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
