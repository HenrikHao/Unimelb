"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import pandas as pd
import requests
from flask import current_app, request, jsonify
import json


import logging

def combine_all_bushfire(start_year, end_year):
    columns = ['Year', 'Month', 'Average PM2.5', 'Average PM10', 'Average Ozone']
    air_quality_year = pd.DataFrame(columns=columns)

    for year in range(start_year, end_year + 1):
        url = f"http://router.fission/getairquality/year/{year}"
        try:
            response = requests.get(url)
            response.raise_for_status()  # Raise an HTTPError for bad responses
            air_quality = response.json()['monthly_data']['buckets']
            for month_data in air_quality:
                new_row = pd.DataFrame({
                    'Year': [year],
                    'Month': [month_data['key_as_string']],
                    'Average PM2.5': [month_data['average_pm2p5']['value']],
                    'Average PM10': [month_data['average_pm10']['value']],
                    'Average Ozone': [month_data['average_ozone']['value']]
                })
                air_quality_year = pd.concat([air_quality_year, new_row], ignore_index=True)

        except Exception as e:
            logging.error(f"Error processing request: {str(e)}")
            continue

    return air_quality_year



def main():
    try:
        start = request.headers['X-Fission-Params-Start']
    except KeyError:
        start = None

    try:
        end = request.headers['X-Fission-Params-End']
    except KeyError:
        end = None

    if (start is not None and end is not None):
        start = int(start)
        end = int(end)
        air_quality_year = combine_all_bushfire(start, end)
    return json.dumps(air_quality_year.to_dict(orient='records'))



if __name__ == "__main__":
    main()
    

