"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

from upload_csv_elasticsearch import *
import pandas as pd
import os
import re

def preprocess_resp(csv_file_path):
    """
    Preprocess the respiratory disease csv file
    Args:
        csv_file_path: path to the csv file
    """
    # Extract filename information
    filename = os.path.basename(csv_file_path)
    match = re.match(r'respiratory_admissions_(?P<sex>females|males)_pha_(?P<year>\d{4})_\d{2}', filename)
    if match:
        sex = match.group('sex')
        year = int(match.group('year'))
    else:
        sex = None
        year = None
    
    # Read CSV file into a pandas DataFrame
    df = pd.read_csv(csv_file_path)
    
    # Add 'sex' and 'year' columns based on filename
    df['sex'] = sex
    df['year'] = year
    if 'pha_code' in df.columns: 
        df.rename(columns={'pha_code': 'pha'}, inplace=True)
    
    # Check if both respiratory and asthma-related columns are present
    if 'resp_asr_per_100k' in df.columns and 'asthma_asr_per_100k' in df.columns:
        # Duplicate rows and rename columns
        resp_df = df.copy()
        resp_df['asr_per_100k'] = resp_df['resp_asr_per_100k']
        resp_df['disease'] = 'Respiratory disease'
        resp_df['population'] = resp_df['resp_count']
        
        asthma_df = df.copy()
        asthma_df['asr_per_100k'] = asthma_df['asthma_asr_per_100k']
        asthma_df['disease'] = 'Asthma'
        asthma_df['population'] = asthma_df['asthma_count']
        
        # Concatenate both DataFrames
        preprocessed_df = pd.concat([resp_df, asthma_df], ignore_index=True)
        preprocessed_df = preprocessed_df.drop(columns=['resp_asr_per_100k','asthma_asr_per_100k','resp_count', 'asthma_count'])
    else:
        # No need for duplication, directly rename columns
        if 'resp_asr_per_100k' in df.columns:
            df['asr_per_100k'] = df['resp_asr_per_100k']
            df['disease'] = 'Respiratory disease'
            df.rename(columns={'resp_count': 'population'}, inplace=True)
            preprocessed_df = df.drop(columns=['resp_asr_per_100k'])
        elif 'asthma_asr_per_100k' in df.columns:
            df['asr_per_100k'] = df['asthma_asr_per_100k']
            df['disease'] = 'Asthma'
            df.rename(columns={'asthma_count': 'population'}, inplace=True)
            preprocessed_df = df.drop(columns=['asthma_asr_per_100k'])
        else: 
            preprocessed_df = df
    
    preprocessed_df.to_csv(csv_file_path, index=False)


def main():
    try: 
        # Connect to Elasticsearch
        es = Elasticsearch (
            'https://127.0.0.1:9200',
            verify_certs= False,
            ssl_show_warn= False,
            basic_auth=('elastic', 'elastic'), 
            request_timeout=100
        )

        csv_directory = os.path.join(os.path.dirname(__file__), 'dataset', 'health_csv_files')
        index_settings = {
            "settings": {
                "index": {
                    "number_of_shards": 3,
                    "number_of_replicas": 2
                }
            },
            "mappings": {
                "properties": {
                    'pha': {'type': 'keyword'},
                    'asr_per_100k': {'type': 'double'},
                    'population': {'type': 'integer'},
                    'proportion': {'type': 'double'},
                    'sex': {'type': 'keyword'},
                    'disease': {'type': 'keyword'},
                    'year': {'type': 'integer'},
                    'month': {'type': 'integer'},
                    'age': {'type': 'keyword'}
                }
            }
        }
        
        # Admissions index (by pha)
        admissions_index_name = 'hospital_admissions_pha'
        create_index(es, admissions_index_name, index_settings)
        for filename in os.listdir(csv_directory):
            csv_file_path = os.path.join(csv_directory, filename)
            if filename.startswith("respiratory_admissions") and filename.endswith('18.csv'):
                preprocess_resp(csv_file_path)
                upload_csv_to_elasticsearch(es, csv_file_path, admissions_index_name, index_settings)
        
        # Admissions index (Australia)
        australia_index_name = 'hospital_admissions_aus'
        create_index(es, australia_index_name, index_settings)
        for filename in os.listdir(csv_directory):
            csv_file_path = os.path.join(csv_directory, filename)
            if filename.startswith("asthma_codp_count") and filename.endswith('.csv'):
                upload_csv_to_elasticsearch(es, csv_file_path, australia_index_name, index_settings)
        
        # Admission index (NSW by age and month)
        nsw_index_name = 'hospital_admissions_nsw'
        create_index(es, nsw_index_name, index_settings)
        for filename in os.listdir(csv_directory):
            csv_file_path = os.path.join(csv_directory, filename)
            if filename.startswith("respiratory_age_2016_2022") and filename.endwith('.csv'): 
                upload_csv_to_elasticsearch(es, csv_file_path, nsw_index_name, index_settings)
    except es_exceptions.ConnectionError as e:
        print(f'Failed to connect to elasticsearch: {e}')
if __name__ == "__main__":
    main()
