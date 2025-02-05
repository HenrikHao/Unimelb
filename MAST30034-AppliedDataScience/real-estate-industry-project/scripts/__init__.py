import download

# dataset URL
sa2_shapefile_url = 'https://www.abs.gov.au/statistics/standards/australian-statistical-geography-standard-asgs-edition-3/jul2021-jun2026/access-and-downloads/digital-boundary-files/SA2_2021_AUST_SHP_GDA2020.zip'
lga_shapefile_url = 'https://www.abs.gov.au/statistics/standards/australian-statistical-geography-standard-asgs-edition-3/jul2021-jun2026/access-and-downloads/digital-boundary-files/LGA_2021_AUST_GDA2020_SHP.zip'
census_2021_url = 'https://www.abs.gov.au/census/find-census-data/datapacks/download/2021_GCP_LGA_for_VIC_short-header.zip'
census_2016_url = 'https://www.abs.gov.au/census/find-census-data/datapacks/download/2016_GCP_LGA_for_VIC_short-header.zip'

# dowanload dataset above
download.download_and_extract_zip(sa2_shapefile_url, "../data/shapefile/SA2")
download.download_and_extract_zip(lga_shapefile_url, "../data/shapefile/LGA")
download.download_and_extract_zip(census_2021_url, "../data/landing/census2021")
download.download_and_extract_zip(census_2016_url, "../data/landing/census2016")