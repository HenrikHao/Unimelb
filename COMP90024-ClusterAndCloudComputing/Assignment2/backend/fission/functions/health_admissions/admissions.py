"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import logging, json
from flask import current_app, request
from elasticsearch8 import Elasticsearch
from string import Template

year_expr = Template('''{
                        "term": {
                        "year": {
                            "value": "${year}"
                        }
                    }}''')
pha_expr = Template('''{
                        "term": {
                        "pha": {
                            "value": "${pha}"
                        }
                    }}''')
pha_year_expr = Template('''{
                            "bool": {
                            "must": [
                                {
                                "term": {
                                    "pha": {
                                    "value": "${pha}"
                                    }
                                }
                                },
                                {
                                "term": {
                                    "year": {
                                    "value": "${year}"
                                    }
                                }
                                }
                            ],
                            "boost": 1
                        }}''')

year_disease_expr = Template(''' {
                             "bool": {
                                "must": [
                                    {
                                    "term": {
                                        "year": {
                                        "value": "${year}"
                                        }
                                    }
                                    },
                                    {
                                    "wildcard": {
                                        "disease": {
                                        "wildcard": "*${disease}*",
                                        "boost": 1
                                        }
                                    }
                                    }
                                ],
                                "boost": 1
                                }
                             }''')
pha_disease_expr = Template(''' {
                                "bool": {
                                "must": [
                                    {
                                    "term": {
                                        "pha": {
                                        "value": "${pha}"
                                        }
                                    }
                                    },
                                    {
                                    "wildcard": {
                                        "disease": {
                                        "wildcard": "*${disease}*",
                                        "boost": 1
                                        }
                                    }
                                    }
                                ],
                                "boost": 1
                                }
                            }''')
pha_year_disease_expr = Template(''' {
                                "bool": {
                                    "must": [
                                        {
                                        "bool": {
                                            "must": [
                                            {
                                                "term": {
                                                "year": {
                                                    "value": "${year}"
                                                }
                                                }
                                            },
                                            {
                                                "wildcard": {
                                                "disease": {
                                                    "wildcard": "*${disease}*",
                                                    "boost": 1
                                                }
                                                }
                                            }
                                            ],
                                            "boost": 1
                                        }
                                        },
                                        {
                                        "term": {
                                            "pha": {
                                            "value": "${pha}"
                                            }
                                        }
                                        }
                                    ],
                                    "boost": 1
                                    }
                                }''')

nsw_month_expr = Template(''' {
                             "bool": {
                                "must": [
                                    {
                                    "term": {
                                        "year": {
                                        "value": "${year}"
                                        }
                                    }
                                    },
                                    {
                                    "term": {
                                        "month": {
                                        "value": "${month}"
                                        }
                                    }
                                    }
                                ],
                                "boost": 1
                                }
                             }''')
nsw_month_disease_expr = Template(''' {
                                "bool": {
                                    "must": [
                                        {
                                        "bool": {
                                            "must": [
                                            {
                                                "term": {
                                                "year": {
                                                    "value": "${year}"
                                                }
                                                }
                                            },
                                            {
                                                "wildcard": {
                                                "disease": {
                                                    "wildcard": "*${disease}*",
                                                    "boost": 1
                                                }
                                                }
                                            }
                                            ],
                                            "boost": 1
                                        }
                                        },
                                        {
                                        "term": {
                                                "month": {
                                                    "value": "${month}"
                                                }
                                            }
                                        }
                                    ],
                                    "boost": 1
                                    }
                                }''')

def main():
    try:
        index = request.headers['X-Fission-Params-Index']
    except:
        index = None
    try:
        year= request.headers['X-Fission-Params-Year']
    except KeyError:
        year= None
    
    try:
        month= request.headers['X-Fission-Params-Month']
    except KeyError:
        month= None

    try:
        pha= request.headers['X-Fission-Params-Pha']
    except KeyError:
        pha= None
    
    try:
        disease= request.headers['X-Fission-Params-Disease']
    except KeyError:
        disease= None

    client = Elasticsearch (
        'https://elasticsearch-master.elastic.svc.cluster.local:9200',
        verify_certs= False,
        basic_auth=('elastic', 'elastic')
    )

    index = 'hospital_admissions*'
    expr ='''{"match_all": {}}'''
    # for nsw data by month
    if year and month: 
        index = 'hospital_admissions_nsw'
        if not disease: 
            expr = nsw_month_expr.substitute(year=year,month=month)
        else: 
            expr = nsw_month_disease_expr.substitute(year=year,month=month,disease=disease)
    elif not year and pha: 
        index = 'hospital_admissions_pha'
        if not disease: 
            expr = pha_expr.substitute(pha=pha)
        else: 
            expr = pha_disease_expr.substitute(pha=pha, disease=disease)
    elif not pha and year: 
        if not disease and not month: 
            expr = year_expr.substitute(index = index, year=year)
    elif year and pha: 
        index = 'hospital_admissions_pha'
        if not disease: 
            expr = pha_year_expr.substitute(pha=pha, year=year)
        else: 
            expr = pha_year_disease_expr.substitute(pha=pha, year=year, disease=disease)

    try: 
        res= client.search(
            index=index,
            query=json.loads(expr),
            size=10000
            )
        return json.dumps(res['hits']['hits'])
    except Exception as e: 
        error_message = {"error": str(e)}
        return json.dumps(error_message)
    
    
