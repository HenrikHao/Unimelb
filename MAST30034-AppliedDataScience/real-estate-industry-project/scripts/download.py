import zipfile, urllib.request, shutil
import contextlib
import os
import urllib.request
import shutil
import zipfile
import contextlib
import os

def download_and_extract_zip(url, destination_folder, zip_file_name=None):
    """
    Download and extract a ZIP file from a given URL to a specified destination folder.

    Args:
    - url (str): The URL of the ZIP file to be downloaded.
    - destination_folder (str): The folder where the ZIP contents should be extracted.
    - zip_file_name (str, optional): The name of the temporary ZIP file. If not provided, the name will be inferred from the URL.

    Returns:
    None
    """

    # If no file name is provided, infer from the URL.
    if zip_file_name is None:
        zip_file_name = url.split('/')[-1]
    
    print("Starting download...")

    with urllib.request.urlopen(url) as response, open(zip_file_name, 'wb') as out_file:
        shutil.copyfileobj(response, out_file)
        with zipfile.ZipFile(zip_file_name) as zf:
            zf.extractall(destination_folder)

    # Remove the zip file from the directory
    with contextlib.suppress(FileNotFoundError):
        os.remove(zip_file_name)

    print('Download complete.')
