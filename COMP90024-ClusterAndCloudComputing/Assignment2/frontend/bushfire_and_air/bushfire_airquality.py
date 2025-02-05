"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

pip install seaborn
pip install folium

import requests
import pandas as pd
import requests
import matplotlib.pyplot as plt
import seaborn as sns
import geopandas as gpd
from shapely.wkt import loads
import folium


def plot_annual_bushfire_trends(bushfire_df):
    '''Define a function to plot annual bushfire trends
    Parameters:
    - bushfire_df: A dataframe about bushfire
    Returns:
        None, but will show plots
    '''
    # Ensure ignition_date is in datetime format and set as index
    if not pd.api.types.is_datetime64_any_dtype(bushfire_df['ignition_date']):
        bushfire_df['ignition_date'] = pd.to_datetime(bushfire_df['ignition_date'])
    if bushfire_df.index.name != 'ignition_date':
        bushfire_df = bushfire_df.set_index('ignition_date')
        
    # Calculate annual total fire area and number of fires
    annual_fire_area = bushfire_df['area_ha'].resample('A').sum()
    annual_fires = bushfire_df.resample('A').size()

    # Create subplots
    fig, axs = plt.subplots(2, 1, figsize=(6, 8))
    
    # Plot for total fire area
    axs[0].plot(annual_fire_area, label='Total Fire Area', color='red')
    axs[0].set_title('Annually Total Fire Area in NSW')
    axs[0].set_ylabel('Total Fire Area (hectares)')
    axs[0].grid(True)

    # Plot for number of fires
    axs[1].plot(annual_fires, label='Number of Fires', color='blue')
    axs[1].set_title('Annually Fire Occurrences in NSW')
    axs[1].set_xlabel('Year')
    axs[1].set_ylabel('Number of Fires')
    axs[1].grid(True)

    # Show the plots
    plt.tight_layout()
    plt.show()
    return

def plot_pivot_table_annually_monthly(bushfire_df):
    '''Define a function to plot fire occurences per year per month
    Parameters:
    - bushfire_df: A dataframe about bushfire
    Returns:
        None, but will show plots
    '''
    # extract year and month
    if not pd.api.types.is_datetime64_any_dtype(bushfire_df['ignition_date']):
        bushfire_df['ignition_date'] = pd.to_datetime(bushfire_df['ignition_date'])
    if bushfire_df.index.name != 'ignition_date':
        bushfire_df = bushfire_df.set_index('ignition_date')

    bushfire_df['year'] = bushfire_df.index.year
    bushfire_df['month'] = bushfire_df.index.month

    # frequency of bushfire per month
    fire_counts = bushfire_df.pivot_table(index='year', columns='month', aggfunc='size', fill_value=0)

    plt.figure(figsize=(8, 6))
    sns.heatmap(fire_counts, annot=True, fmt="d", cmap='YlOrRd', linewidths=.5)
    plt.title('Fire Occurrences by Month and Year')
    plt.xlabel('Month')
    plt.ylabel('Year')
    plt.show()

    return


def combine_all_bushfire(start_year, end_year):
    '''Define a function to combine the average air quality measure all year and month
    Parameters:
    - start_year: the start year we want
    - enf_year: the end year we want
    Returns:
    - air_quality_year: a dataframe recording average pm25, pm10 and ozone in each month from 
                        start_year to end_year
    '''
    
    # initialize a DataFrame
    columns = ['Year', 'Month', 'Average PM2.5', 'Average PM10', 'Average Ozone']
    air_quality_year = pd.DataFrame(columns=columns)

    for year in range(start_year, end_year+1):
        url = f"http://localhost:9090/getairquality/year/{year}"
        air_quality = (requests.get(url)).json()['monthly_data']['buckets']
        for year_air_quaility in air_quality:
            year = year
            month = year_air_quaility['key_as_string']
            avg_pm25 = year_air_quaility['average_pm2p5']['value']
            avg_pm10 = year_air_quaility['average_pm10']['value']
            avg_ozone = year_air_quaility['average_ozone']['value']
        
            new_row = {'Year': year, 'Month': month, 'Average PM2.5': avg_pm25, 'Average PM10': avg_pm10, 'Average Ozone': avg_ozone}
            air_quality_year = air_quality_year.append(new_row, ignore_index=True)

    return air_quality_year


def air_quality_measure_annually_monthly(air_quality_year):
    pivot_pm25 = air_quality_year.pivot_table(index='Year', columns='Month', values='Average PM2.5', aggfunc='mean')
    pivot_pm10 = air_quality_year.pivot_table(index='Year', columns='Month', values='Average PM10', aggfunc='mean')
    pivot_ozone = air_quality_year.pivot_table(index='Year', columns='Month', values='Average Ozone', aggfunc='mean')

    sns.set(style="white")
    # PM2.5
    plt.figure(figsize=(8, 6))
    sns.heatmap(pivot_pm25, annot=True, cmap='YlOrRd', fmt=".2f")
    plt.title('Annual and Monthly Average PM2.5')
    plt.show()

    # PM10
    plt.figure(figsize=(8, 6))
    sns.heatmap(pivot_pm10, annot=True, cmap='YlOrRd', fmt=".2f")
    plt.title('Annual and Monthly Average PM10')
    plt.show()

    # Ozone
    plt.figure(figsize=(8, 6))
    sns.heatmap(pivot_ozone, annot=True, cmap='YlOrRd', fmt=".2f")
    plt.title('Annual and Monthly Average Ozone')
    plt.show()
    return


def find_site_each_year(year):
    columns = ['siteid', 'phacode', 'timestamp', 'geometry', 'sitename', 'pm10', 'pm2p5', 'ozone']
    site_year = (requests.get(f'http://localhost:9090/getairquality/yearsites/{year}')).json()
    site_list = []
    for site in site_year:
        site_list.append(site['_source'].values())
    site_year_clean = pd.DataFrame(site_list, columns=columns).dropna(subset=['pm10', 'pm2p5', 'ozone'], how='all')
    site_year_clean['timestamp'] = pd.to_datetime(site_year_clean['timestamp'])
    site_year_clean = site_year_clean.set_index('timestamp')
    site_year_clean['year'] = site_year_clean.index.year
    site_year_clean['month'] = site_year_clean.index.month

    site_year_clean['geometry'] = site_year_clean['geometry'].apply(loads)
    site_year_clean = gpd.GeoDataFrame(site_year_clean, geometry='geometry')
    site_year_clean = site_year_clean.set_crs("EPSG:4326")

    return site_year_clean


def find_near_far_site(year, index_name, bushfire_df):
    '''Define a function to find the airquality of nearest site and farthest site
    Parameters:
    - year: which year we want
    - index_name: can be pm2p5, pm10 or ozone
    - bushfire_df: bushfire dataframe
    Returns:
    - nearest_farthest_data: a dataframe containing concentration of the nearest and farthest site,
                also with some information like site name, location, date etc.
    '''
    site_year_df = find_site_each_year(year)

    bushfire_df['ignition_date'] = pd.to_datetime(bushfire_df['ignition_date'])
    bushfire_df = bushfire_df.set_index('ignition_date')
    bushfire_df['year'] = bushfire_df.index.year
    bushfire_df['month'] = bushfire_df.index.month
    bushfire_df['geometry'] = bushfire_df['geometry'].apply(loads)
    bushfire_gdf = gpd.GeoDataFrame(bushfire_df, geometry='geometry')
    bushfire_gdf = bushfire_gdf.set_crs("EPSG:4326")
    
    bushfire_year_gdf = bushfire_gdf[bushfire_gdf['year']==year]
    merged_gdf = pd.merge(site_year_df, bushfire_year_gdf, on='month')

    points = merged_gdf[['month', 'siteid', 'geometry_x', index_name]].copy().drop_duplicates()
    polygons = merged_gdf[['month', 'geometry_y', 'area_ha']].copy().drop_duplicates()

    nearest_farthest_data = pd.DataFrame()

    # only compare distance under the same month 
    for month in polygons['month'].unique():
        month_polygons = polygons[polygons['month'] == month]
        month_points = points[points['month'] == month]
        
        for index, poly in month_polygons.iterrows():
            # calculate distance between site and fire polygon
            month_points['distance'] = month_points.apply(lambda row: row['geometry_x'].distance(poly['geometry_y']), axis=1)
            # find the nearest and farthest site
            nearest_point = month_points.loc[month_points['distance'].idxmin()]
            farthest_point = month_points.loc[month_points['distance'].idxmax()]

            nearest_farthest_data = nearest_farthest_data.append({
                'Site_loc': nearest_point['geometry_x'],
                'Fire_loc': poly['geometry_y'],
                'month': month,
                'area': poly['area_ha'],
                'Nearest Site ID': nearest_point['siteid'],
                f'{index_name}_near': nearest_point[index_name],
                f'{index_name}_far': farthest_point[index_name],
                'Farthest Site ID': farthest_point['siteid'],
            }, ignore_index=True)

    return nearest_farthest_data


def bushfire_site_box_plot(year, index, bushfire_df):
    '''Define a function to compare the airquality of nearest site and farthest site
     through box plots
    Parameters:
    - year: which year we want
    - index: can be pm2p5, pm10 or ozone
    - bushfire_df: bushfire dataframe
    Returns:
    None, but showing the box plot of nearest site and farthest site concentration
    '''
     
    ### large range bushfire
    near_far_year = find_near_far_site(year, index, bushfire_df)
    large_bushfire = near_far_year[near_far_year['area'] > 100]
    melted_large = large_bushfire.melt(id_vars=['month', 'Site_loc', 'Fire_loc', 'area', 'Nearest Site ID'], 
                        value_vars=[f'{index}_near', f'{index}_far'], 
                        var_name='Measurement', value_name='Value')

    ### small range bushfire
    near_far_year = find_near_far_site(year, index, bushfire_df)
    small_bushfire = near_far_year[near_far_year['area'] <= 5]
    melted_small = small_bushfire.melt(id_vars=['month', 'Site_loc', 'Fire_loc', 'area', 'Nearest Site ID'], 
                        value_vars=[f'{index}_near', f'{index}_far'], 
                        var_name='Measurement', value_name='Value')


    plt.figure(figsize=(6, 4))
    sns.boxplot(x='month', y='Value', hue='Measurement', data=melted_small)
    plt.title(f'Monthly Comparison of {index}_near and {index}_far for small bushfire')
    plt.xlabel('Month')
    plt.ylabel('Concentration')
    plt.legend(title='Measurement')
    plt.grid(True)
    plt.show()


    plt.figure(figsize=(6, 4))
    sns.boxplot(x='month', y='Value', hue='Measurement', data=melted_large)
    plt.title(f'Monthly Comparison of {index}_near and {index}_far for large bushfire')
    plt.xlabel('Month')
    plt.ylabel('Concentration')
    plt.legend(title='Measurement')
    plt.grid(True)
    plt.show()

    return 


def main():
    bushfire = (requests.get('http://localhost:9090/bushfireget')).json()
    bushfire_df = pd.DataFrame(bushfire)
    plot_annual_bushfire_trends(bushfire_df)
    plot_pivot_table_annually_monthly(bushfire_df)
    return

   
    
    

if __name__ == "__main__":
    main()
    
