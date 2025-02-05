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

def main():
    # try:
    #     year= request.headers['X-Fission-Params-Year']
    # except KeyError:
    #     year= None

    # try:
    #     pha= request.headers['X-Fission-Params-Pha']
    # except KeyError:
    #     pha= None
    
    # try:
    #     disease= request.headers['X-Fission-Params-Disease']
    # except KeyError:
    #     disease= None
    
    year = 2014
    disease = "Respi"
    pha = None

    client = Elasticsearch (
        'https://127.0.0.1:9200',
        verify_certs= False,
        basic_auth=('elastic', 'elastic')
    )

    if year is None and pha is not None: 
        if disease is None: 
            expr = pha_expr.substitute(pha=pha)
        else: 
            expr = pha_disease_expr.substitute(pha=pha, disease=disease)
    elif pha is None and year is not None: 
        if disease is None: 
            expr = year_expr.substitute(year=year)
        else: 
            expr = year_disease_expr.substitute(year=year, disease=disease)
    elif year is not None and pha is not None: 
        if disease is None: 
            expr = pha_year_expr.substitute(pha=pha, year=year)
        else: 
            expr = pha_year_disease_expr.substitute(pha=pha, year=year, disease=disease)
    
    print("requesting from elasticsearch")
    
    result_list=[]
    res= client.search(
      index='hospital_admissions',
      query=json.loads(expr),
      size=10000
    )
    
    for hit in res['hits']['hits']:
        result_list.append(hit['_source'])
    print(result_list)
    print(len(result_list))
    return result_list

if __name__ == "__main__":
    main()