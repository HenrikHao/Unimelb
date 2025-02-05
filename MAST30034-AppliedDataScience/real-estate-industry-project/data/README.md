# Datasets

## Real Estate Data
We scraped 12014 properties from domain.

## Census Data
We used Median Weekly income data from the census data [2021](https://www.abs.gov.au/census/find-census-data/datapacks/download/2021_GCP_LGA_for_VIC_short-header.zip) and [2016](https://www.abs.gov.au/census/find-census-data/datapacks/download/2016_GCP_LGA_for_VIC_short-header.zip).

## Offence Count Data
The offence count data is published by Crime Statistics Agency and is available [here](https://www.crimestatistics.vic.gov.au/crime-statistics/latest-victorian-crime-data/download-data). Note that the Offence Count is in Excel format, to ensure it can be read appropriately, we manually deleted some irrelevant rows. The data is stored in `raw/offence_count.xlsx` as it was manually preprocessed.

## Population Forecast Data
The population forecast data is published by Australian Institute of Health and Welfare and can be found [here](https://www.gen-agedcaredata.gov.au/Resources/Access-data/2019/September/Population-projections,-2017-(base)-to-2032-for-al). The forecasts is from 2017 to 2032.

## Historical Rent Data
The historical rent data is published by Homes Victoria and can be found [here](https://discover.data.vic.gov.au/dataset/rental-report-quarterly-moving-annual-rents-by-suburb). Please note that this dataset is also in excel format so we have also manually deleted some irrelevant rows and columns. The data is stored in `raw/Quarterly median rents by local government area - March quarter 2023.xlsx` as it was manually preprocessed.

## PTV Station Data
The PTV station data was found on PTV website: [metro](https://discover.data.vic.gov.au/dataset/ptv-metro-train-stations) and [regional](https://discover.data.vic.gov.au/dataset/ptv-regional-train-stations)

## Facilities Location Data
Location data including hospital, school, park, etc, are collected from overpass turbo.

## Proximity
We leveraged Open Route Service within Docker to calculate proximities, please read this detailed [guide](https://giscience.github.io/openrouteservice/installation/Running-with-Docker.html) for set up.
