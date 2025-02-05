"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)

This file is from https://gitlab.unimelb.edu.au/feit-comp90024/comp90024/-/blob/master/test/4th-iteration/functions/library/Commons.py
"""

class Commons:

    @staticmethod
    def config(k):
        with open(f'/configs/default/parameters/{k}', 'r') as f:
            return f.read()

    @staticmethod
    def auth():
        return (Commons.config("ES_USERNAME"), Commons.config("ES_PASSWORD"))

    # @staticmethod
    # def search_url():
    #     return f'{Commons.config("ES_URL")}/{Commons.config("ES_DATABASE")}/_search'

