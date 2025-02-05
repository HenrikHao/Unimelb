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
# To use this getter, you can run the following commands
'''
curl "http://localhost:9090/getairquality/site/{siteid}/year/{year}/{month}" | jq '.' returning the airquality info of a siteid given the month
curl "http://localhost:9090/getaverageairquality/site/{siteid}/month/{month}" returning average airquality given a site and a month across all years (month format in 01 to 12)
(for example, curl "http://localhost:9090/getaverageairquality/33/01 will return the average air quality for site 33 and month 01 across all years (2013 to 2021 for example))
curl "http://localhost:9090/getaverageairquality/year/{year}/month/{month} returning the avearage air quality across all sites given a year and a month
curl "http://localhost:9090/getaverageairquality/month/{month} (month format in 01 to 12) returning the average air quality across all sites across all years
'''

def main():
    try:
        siteid = request.headers['X-Fission-Params-Siteid']
    except:
        siteid = None

    try:
        year = request.headers['X-Fission-Params-Year']
    except KeyError:
        year = None
    
    try:
        month = request.headers['X-Fission-Params-Month']
    except KeyError:
        month = None

    try:
        year_sites = request.headers['X-Fission-Params-Yearsites']
    except KeyError:
        year_sites = None
    
    index = "airquality"
    try: 
        test = request.headers['X-Fission-Params-Test']
        index = "test_air_quality"
    except KeyError:
        test = False

    # Setup Elasticsearch client
    client = Elasticsearch(
        'https://elasticsearch-master.elastic.svc.cluster.local:9200',
        verify_certs=False,
        basic_auth=('elastic', 'elastic')
    )

    try:
        if siteid and year and month:
            query = {
                "query": {
                    "bool": {
                        "must": [
                            {
                                "term": {
                                    "siteid": siteid
                                }
                            },
                            {
                                "range": {
                                    "timestamp": {
                                        "gte": f"{year}-{month}",  # Start of the month
                                        "lte": f"{year}-{month}",  # End of the month
                                        "format": "yyyy-MM"
                                    }
                                }
                            }
                        ]
                    }
                }
            }
            response = client.search(index=index, body=query)
        elif siteid and month: # returning average airquality of that month across all years
            query = {
                "size": 0, 
                "query": {
                    "bool": {
                        "must": [
                            {
                                "term": {
                                    "siteid": siteid
                                }
                            },
                            {
                                "script": {
                                    "script": {
                                        "lang": "painless",
                                        "source": "doc['timestamp'].value.getMonthValue() == params.month",
                                        "params": {
                                            "month": int(month)
                                        }
                                    }
                                }
                            }
                        ]
                    }
                },
                "aggs": {
                    "average_pm10": {
                        "avg": {
                            "field": "pm10"
                        }
                    },
                    "average_pm2p5": {
                        "avg": {
                            "field": "pm2p5"
                        }
                    },
                    "average_ozone": {
                        "avg": {
                            "field": "ozone"
                        }
                    }
                }
            }
            response = client.search(index=index, body=query)
        
        elif year and month:
            query = {
                "size": 0,
                "query": {
                    "range": {
                        "timestamp": {
                            "gte": f"{year}-{month}",  # Start of the month
                            "lte": f"{year}-{month}",  # End of the month
                            "format": "yyyy-MM"
                        }
                    }
                },
                "aggs": {
                    "average_pm10": {
                        "avg": {
                            "field": "pm10"
                        }
                    },
                    "average_pm2p5": {
                        "avg": {
                            "field": "pm2p5"
                        }
                    },
                    "average_ozone": {
                        "avg": {
                            "field": "ozone"
                        }
                    }
                }
            }

            # Execute the search query
            response = client.search(index=index, body=query)

        elif year:
            query = {
                "size": 0,
                "query": {
                    "range": {
                        "timestamp": {
                            "gte": f"{year}-01-01T00:00:00",
                            "lte": f"{year}-12-31T23:59:59",
                            "format": "yyyy-MM-dd'T'HH:mm:ss"
                        }
                    }
                },
                "aggs": {
                    "monthly_data": {
                        "date_histogram": {
                            "field": "timestamp",
                            "calendar_interval": "month",
                            "format": "MM"
                        },
                        "aggs": {
                            "average_pm10": {
                                "avg": {
                                    "field": "pm10"
                                }
                            },
                            "average_pm2p5": {
                                "avg": {
                                    "field": "pm2p5"
                                }
                            },
                            "average_ozone": {
                                "avg": {
                                    "field": "ozone"
                                }
                            }
                        }
                    }
                }
            }
            response = client.search(index=index, body=query)
        
        elif month:
            query = {
                "size": 0,
                "query": {
                    "bool": {
                        "filter": [
                            {
                                "script": {
                                    "script": {
                                        "source": "doc['timestamp'].value.getMonthValue() == params.month",
                                        "params": {
                                            "month": int(month)
                                        }
                                    }
                                }
                            }
                        ]
                    }
                },
                "aggs": {
                    "average_pm10": {
                        "avg": {
                            "field": "pm10"
                        }
                    },
                    "average_pm2p5": {
                        "avg": {
                            "field": "pm2p5"
                        }
                    },
                    "average_ozone": {
                        "avg": {
                            "field": "ozone"
                        }
                    }
                }
            }
            response = client.search(index=index, body=query)

        elif year_sites:
            query = {
                "query": {
                    "range": {
                    "timestamp": {
                        "gte": f"{year_sites}-01-01",
                        "lte": f"{year_sites}-12-31",
                        "format": "yyyy-MM-dd"
                    }
                    }
                },
                "sort": [
                    {
                    "timestamp": {
                        "order": "asc"
                    }
                    }
                ],
                "size": 1000
            }
            response = client.search(index=index, body=query)
        else:
            return json.dumps({"error": "Invalid request parameters"})

        return json.dumps(response['aggregations'] if 'aggregations' in response else response['hits']['hits'])
    except Exception as e:
        logging.error(f"Error processing request: {str(e)}")
        return json.dumps({"error": str(e)})



