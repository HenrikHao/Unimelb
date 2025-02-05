import re
import time
from json import dump
from collections import defaultdict
from bs4 import BeautifulSoup
import requests

# Constants
BASE_URL = "https://www.domain.com.au"
MAX_PAGES = 50
HEADERS = {
    "User-Agent": "Mozilla/5.0 (X11; CrOS x86_64 12871.102.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.141 Safari/537.36"
}

url_links = []
property_metadata = defaultdict(dict)

def make_request(url):
    """
    Make a GET request to a given URL and return the BeautifulSoup object.
    
    Parameters:
    - url (str): The URL to make a request to.
    
    Returns:
    - BeautifulSoup object: Parsed HTML content of the URL.
    """
    try:
        response = requests.get(url, headers=HEADERS, timeout=60)
        return BeautifulSoup(response.text, "lxml")
    except Exception as e:
        print(f"Error while fetching {url}. Error: {e}")
        return None

# Loop through postcodes and pages to scrape property URLs
for postcode in range(3000, 4000):
    for page in range(1, MAX_PAGES + 1):
        url = BASE_URL + f"/rent/?excludedeposittaken=1&postcode={postcode}&page={page}"
        print(f"Visiting {url}")
        
        bs_object = make_request(url)
        
        if not bs_object:
            print(f"Error accessing {url}. Skipping...")
            continue

        try:
            index_links = bs_object \
                .find("ul", {"data-testid": "results"}) \
                .findAll("a", href=re.compile(f"{BASE_URL}/*"))
            
            if not index_links:
                print(f"No listings found on page {page} for postcode {postcode}.")
                break

            # Extracting property links...
            for link in index_links:
                if 'address' in link['class']:
                    url_links.append(link['href'])

            # Sleep for 5 seconds between page requests to avoid rate limits
            time.sleep(5)

        except AttributeError as e:
            print(f"Unexpected page structure at {url}. Error: {e}")
            break

# Loop through property URLs to extract metadata
for property_url in url_links:
    bs_object = make_request(property_url)
    if not bs_object:
        continue

    try:
        property_metadata[property_url]['name'] = bs_object \
            .find("h1", {"class": "css-164r41r"}) \
            .text

        property_metadata[property_url]['cost_text'] = bs_object \
            .find("div", {"data-testid": "listing-details__summary-title"}) \
            .text

        property_metadata[property_url]['coordinates'] = [
            float(coord) for coord in re.findall(
                r'destination=([-\s,\d\.]+)',
                bs_object \
                    .find(
                        "a",
                        {"target": "_blank", 'rel': "noopener noreferrer"}
                    ) \
                    .attrs['href']
            )[0].split(',')
        ]

        rooms_list = []
        for feature in bs_object \
                .find("div", {"data-testid": "property-features"}) \
                .findAll("span", {"data-testid": "property-features-text-container"}):
            try:
                rooms_list.append(re.findall(r'\d\s[A-Za-z]+', feature.text)[0])
            except IndexError:
                pass

        property_metadata[property_url]['rooms'] = rooms_list
        property_metadata[property_url]['desc'] = bs_object.find("p").text

    except AttributeError:
        print(f"Issue with {property_url}, see raw dump below")
        print(bs_object.text)
        continue

    # Sleep for 5 seconds between individual property requests
    time.sleep(5)

# Save the scraped metadata to a JSON file
with open('../data/landing/property.json', 'w') as f:
    dump(property_metadata, f)
