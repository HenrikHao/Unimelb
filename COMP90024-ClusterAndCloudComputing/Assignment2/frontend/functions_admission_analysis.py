"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import requests
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.colors import Normalize
from matplotlib.cm import ScalarMappable
import geopandas as gpd
import numpy as np
import geopandas as gpd
import re
from mpl_toolkits.axes_grid1 import make_axes_locatable

# Initialize an empty list to store the fetched NSW data
nsw_data = []

'''Define a function to fetch NSW data for the specified years
Parameters:
- years: A list of strings representing years (e.g., ["2016", "2017"])
Returns:
- nsw_data: A list containing the fetched NSW data'''

def yearly_nsw_data(years):
    for year in years:
        response = requests.get("http://localhost:9090/admissions/years/" + year)
        data = response.json()
        df = pd.DataFrame(data)
        # Filter the DataFrame to include only entries related to NSW hospital admissions
        df = df[df["_index"] == "hospital_admissions_nsw"]
        for i in df["_source"]:
            nsw_data.append(i)
    nsw_data_df = pd.DataFrame.from_records(nsw_data)
    nsw_data_df["population"] = nsw_data_df["population"].astype(int)
    nsw_data_df["month"] = nsw_data_df["month"].astype(int)
    nsw_data_df["year"] = nsw_data_df["year"].astype(int)
    return nsw_data_df

# Initialize an empty list to store the fetched air quality data
air_quality = []

'''Define a function to fetch air quality data for the specified years
Parameters:
- years: A list of strings representing years (e.g., ["2016", "2017"])
Returns:
- air_quality: A list containing the fetched air quality data'''
def yearly_air_quality(years):
    for year in years:
        response = requests.get("http://localhost:9090/getairquality/year/"+year)
        data = response.json()
        df = pd.DataFrame(data["monthly_data"])
        for i in df["buckets"]:
            i["year"] = year
            air_quality.append(i)
    return air_quality


# Initialize an empty list to store the fetched PHA admission data
pha_admission = []

'''Define a function to fetch PHA admission data for a specific disease in the year 2021
Parameters:
- disease: A string representing the disease for which admission data is to be fetched
Returns:
- pha_admission: A list containing the fetched PHA admission data for the specified disease'''

def pha_admission_data(diease):
    admissions_all = requests.get("http://localhost:9090/admissions/years/2021/diseases/"+diease)
    data = admissions_all.json()
    admissions_all_df = pd.DataFrame(data)
    for i in admissions_all_df["_source"]:
        pha_admission.append(i)
    return pha_admission

'''Define a function to create a bar chart plot
Parameters:
- result: The DataFrame or Series containing the data to be plotted
- x_label: The label for the x-axis
- y_label: The label for the y-axis
- title: The title of the plot'''

def bar_chart_plot(result, x_label, y_label, title):
    result.plot(kind='bar')
    plt.xlabel(x_label)
    plt.ylabel(y_label)
    plt.title(title)
    plt.xticks(rotation=360)
    plt.show()

'''Define a function to perform Min-Max scaling (normalization) on a specified column of a DataFrame
Parameters:
- df: The DataFrame containing the data to be normalized
- col_name: The name of the column to be normalized'''

def normalization(df, col_name):
    # Min-Max scaling (normalization)
    min_val = df[col_name].min()
    max_val = df[col_name].max()
    df[col_name+'_normalized'] = (df[col_name] - min_val) / (max_val - min_val)