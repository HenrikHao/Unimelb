"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

from upload_csv_elasticsearch import *
import os
import csv

# Function to upload pha index from CSV file
def upload_pha(file_path):
    index_name = "hospital_admissions_nsw"
    try:
        # Connect to Elasticsearch
        es = Elasticsearch (
            'https://127.0.0.1:9200',
            verify_certs= False,
            ssl_show_warn= False,
            basic_auth=('elastic', 'elastic')
        )
        with open(file_path, 'r', newline='', encoding='utf-8') as csvfile:
            csvreader = csv.DictReader(csvfile)
            for row in csvreader:
                Year = row['Year']
                Month = row['Month']
                disease = row["Disease"]
                age = row["Age"]
                admission = row["Admissions"]
                Admission_propotion = row["Admission propotion"]
                es.index(index=index_name, body={
                    'Year': Year,
                    'Month': Month,
                    'disease': disease,
                    'age': age,
                    'admission': admission,
                    'Admission_propotion': Admission_propotion
                })
        print(f"Successfully uploaded health_age index")
    except es_exceptions.ConnectionError as e:
        print(f'Connection to Elasticsearch failed: {e}')


def main():
    csv_directory = os.path.join(os.path.dirname(__file__), 'dataset')

    # Specify the Elasticsearch index
    index_name = 'health_age'
    index_body = {
        "mappings": {
            "properties": {
                'Year': {'type': 'integer'},
                'Month': {'type': 'keyword'},
                'disease': {'type': 'keyword'},
                'age': {'type': 'keyword'},
                'admission': {'type': 'integer'},
                'Admission_propotion': {'type': 'double'}
            }
        }
    }

    # Create the index with mappings
    create_index(index_name, index_body)
    csv_file_path = os.path.join(csv_directory, "respiratory_age_2016_2022.csv")
    upload_pha(csv_file_path)
    

if __name__ == "__main__":
    main()

