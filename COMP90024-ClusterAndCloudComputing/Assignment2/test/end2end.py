"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import unittest
import requests
import json, time
from unittest.mock import patch
from flask import request, current_app
import subprocess

class HTTPSession:
    def __init__(self, protocol, hostname, port):
        self.session = requests.Session()
        self.base_url = f'{protocol}://{hostname}:{port}'

    def getURL(self, path): 
        return f'{self.base_url}{path}'

    def get(self, path):
        return self.session.get(f'{self.base_url}/{path}')

    def post(self, path, data):
        return self.session.post(f'{self.base_url}/{path}', data)


    def put(self, path, data):
        return self.session.put(f'{self.base_url}/{path}', data)

    def delete(self, path):
        return self.session.delete(f'{self.base_url}/{path}')

class TestEnd2End(unittest.TestCase):
    def setUp(self):
        '''
        Clear the air quality testing database
        '''
        self.assertEqual(test_request.delete('/wipedatabase').status_code, 200)
        time.sleep(5)

    def test_admission(self):
        '''
        Test the RestFul APIs for hospital admissions index. 
        Confirms the existance of routes, right functions are called, and the right indexes are retrieved.
        '''
        self.assertEqual(test_request.get('/admissions/years/2014').status_code, 200)
        self.assertIn('"_index": "hospital_admissions_pha"', test_request.get('/admissions/years/2014').text)
        
        self.assertEqual(test_request.get('/admissions/nsw/years/2016/months/1').status_code, 200)
        self.assertIn('"_index": "hospital_admissions_nsw"', test_request.get('/admissions/nsw/years/2016/months/1').text)
        
        self.assertEqual(test_request.get('/admissions/nsw/years/2016/months/1').status_code, 200)
        self.assertIn('"_index": "hospital_admissions_nsw"', test_request.get('/admissions/nsw/years/2016/months/1').text)
    
    def test_bushfire(self): 
        '''
        Test the RestFul APIs for bushfire index. 
        Confirms the existance of routes, right functions are called, and the right indexes are retrieved.
        '''
        self.assertEqual(test_request.get('/bushfireget').status_code, 200)
        self.assertIn('ignition_date', test_request.get('/bushfireget').text)

    def test_get_air_quality(self): 
        '''
        Test the RestFul APIs for getting air quality index. 
        Confirms the existance of routes, right functions are called, and the right indexes are retrieved.
        '''
        self.assertEqual(test_request.get('/getairquality/yearsites/2019').status_code, 200)
        self.assertIn('"_index": "airquality"', test_request.get('/getairquality/yearsites/2019').text)
    
    def test_collect_air_quality(self): 
        '''
        Test the RestFul APIs for collecting air quality data into ES.
        '''
        # Run the curl command
        process = subprocess.Popen(
            ['curl', '-X', 'POST', '-v', test_request.getURL('/collect-air-quality-data/test/t/siteid/33/startmonth/2013-01/endmonth/2013-02')],
            stdout=subprocess.PIPE,  # Redirect stdout
            stderr=subprocess.PIPE,  # Redirect stderr
            universal_newlines=True  # Ensure output is decoded as text
        )

        # # Wait for the process to complete
        stdout, stderr = process.communicate()
        time.sleep(5)

        # Get data from ES
        r = test_request.get('/getairquality/test/t/siteid/33/year/2013/month/01')
        self.assertEqual(r.status_code, 200)
        self.assertIsNotNone(r.json())
        #print('\n',r.json(),'\n')
        o = r.json()[0]['_source']
        self.assertEqual(o['siteid'],'33')
        self.assertEqual(o['timestamp'],'2013-01')
        
    def test_combine_bushfire(self): 
        start_year = 2013
        end_year = 2014
        r = test_request.get(f'/combine-bushfire/start/{start_year}/end/{end_year}')
        self.assertEqual(r.status_code, 200)
        self.assertIsNotNone(r.json())
        o = r.json()
        data_within_range = True
        for i in range(len(o)): 
            if o[i]['Year'] < start_year or o[i]['Year'] > end_year: 
                data_within_range = False
                break
        self.assertTrue(data_within_range)
        

if __name__ == '__main__':

    test_request = HTTPSession('http', 'localhost', 9090)
    unittest.main()
