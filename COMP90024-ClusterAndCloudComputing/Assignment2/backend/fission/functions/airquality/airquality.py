"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import pandas as pd
from elasticsearch8 import Elasticsearch
from flask import request
import requests
import json
import time
import os

csv_file_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "aq_site_PHA.csv")
# Load DataFrame globally so it loads once
df = pd.read_csv(csv_file_path)
df['Site_Id'] = df['Site_Id'].astype(int)
df['Code'] = df['Code'].astype(int)

def get_pha_id(site_id, site_pha_df):
    filtered_df = site_pha_df[site_pha_df['Site_Id'] == site_id]
    if not filtered_df.empty:
        code = filtered_df['Code'].iloc[0]
        sitename = filtered_df['SiteName'].iloc[0]
        point = filtered_df['geometry'].iloc[0]
        return code, point, sitename
    return None, None, None

def get_data(site_id, start_month, end_month):
    url = "https://data.airquality.nsw.gov.au/api/Data/get_Observations"
    headers = {"accept": "application/json", "Content-Type": "application/json"}
    params = {
        "Parameters": ["PM2.5", "Ozone", "PM10"],
        "Sites": [site_id],
        "StartDate": start_month,
        "EndDate": end_month,
        "Categories": ["Averages"],
        "SubCategories": ["Monthly"],
        "Frequency": ["Hourly average"]
    }
    response = requests.post(url, headers=headers, data=json.dumps(params))
    if response.status_code != 200:
    # Handle non-OK responses or log them
        return "API response error: {}".format(response.status_code)
    observations = response.json()

    extracted_data = []

    # Temporary storage to aggregate data by date
    temp_storage = {}
    for obs in observations:
        date = obs['Date']
        parameter_code = obs['Parameter']['ParameterCode']
        frequency = obs['Parameter']['Frequency']
        value = obs['Value']
        phacode, geo, site_name = get_pha_id(obs['Site_Id'], df)
        
        if date not in temp_storage:
            temp_storage[date] = {
                'siteid': site_id,
                'phacode': phacode,
                'timestamp': start_month,
                'geo': geo,
                'sitename': site_name,
                'pm10': None,
                'pm2p5': None,
                'ozone': None
            }
        
        if parameter_code == "PM10":
            temp_storage[date]['pm10'] = value
        if parameter_code == "PM2.5":
            temp_storage[date]['pm2p5'] = value
        if parameter_code == "OZONE":
            temp_storage[date]['ozone'] = value

    # Convert temporary storage into a list of records
    extracted_data = list(temp_storage.values())
    return extracted_data

def main():
    try:
        start_month = request.headers['X-Fission-Params-Startmonth']
    except KeyError:
        start_month = None

    try:
        site_id = request.headers['X-Fission-Params-Siteid']
    except KeyError:
        site_id = None
    
    try:
        end_month = request.headers['X-Fission-Params-Endmonth']
    except KeyError:
        end_month = None
    
    try: 
        test = request.headers['X-Fission-Params-Test']
    except KeyError: 
        test = False
    

    try:

        es = Elasticsearch (
        'https://elasticsearch-master.elastic.svc.cluster.local:9200',
        verify_certs= False,
        ssl_show_warn= False,
        basic_auth=('elastic', 'elastic')
        )
        data_to_index = get_data(site_id, start_month, end_month)

        # Insert data into Elasticsearch
        index = "airquality"
        if test: 
            index = "test_air_quality"
        for data in data_to_index:
            es.index(index=index, document=data)

        return "Data collection completed for site {} from {} to {}".format(site_id, start_month, end_month)
    except Exception as e:
        error_message = "Error processing request: {}".format(str(e))
        return error_message