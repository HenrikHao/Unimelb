from mpi4py import MPI
import json
import os
from datetime import datetime

begin_time = datetime.now()
comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

# data structure to store information
happy_hour_dict = {}
happy_day_dict = {}
active_hour_dict = {}
active_day_dict = {}

def find_max_value_key(dict):
    sorted_items = sorted(dict.items(), key=lambda x: x[1], reverse=True)
    return sorted_items[0][0], sorted_items[0][1] # (key, value)

def get_params(tweet): 
    '''
    Getting the relevant info from the twitter file
    '''

    # getting date, time and sentiment
    data = tweet.get('doc').get('data')
    time_obj = datetime.strptime(data.get('created_at'), "%Y-%m-%dT%H:%M:%S.%fZ")
    date = str(time_obj.date())
    time = date+str(time_obj.time())[:2]
    sentiment = data.get('sentiment')

    if isinstance(sentiment, (int, float)):
        sentiment_score = sentiment
    elif isinstance(sentiment, dict):
        sentiment_score = sentiment['score']
    else:
        sentiment_score = None

    return date, time, sentiment_score

# combine the dictionaries
def merge_dict(dict1, dict2):
    for key, value in dict2.items():
        if key in dict1:
            dict1[key] += value
        else:
            dict1[key] = value
    return dict1
        
# divide the whole file into n part, each processor processes one part.
total_bytes = os.path.getsize('./twitter-100gb.json')
each_byte = total_bytes / size
begin_position = rank * each_byte
end_position = (rank + 1) * each_byte
    
with open('./twitter-100gb.json', 'r', encoding='utf-8') as tweet_file:
    tweet_file.seek(begin_position)
    tweet_file.readline() # jump the rest data if the position is not from the start
    skipped_line_length = tweet_file.tell() - begin_position
    tweet_str = tweet_file.readline() # start from a complete tweet row
    current_position = begin_position + skipped_line_length

    while (tweet_str!= '{}]}\n'):
        if (current_position >= end_position): # other rank's task
            break
    
        # store the necessary data
        tweet = json.loads(tweet_str[:-2])
        date, time, sentiment_score = get_params(tweet)
    
        if date not in active_day_dict:
            happy_day_dict[date] = 0
            active_day_dict[date] = 0
        active_day_dict[date] += 1

        if time not in active_hour_dict:
            happy_hour_dict[time] = 0
            active_hour_dict[time] = 0
        active_hour_dict[time] += 1
        
        if sentiment_score:
            happy_day_dict[date] += sentiment_score
            happy_hour_dict[time] += sentiment_score

        byte_count = len((tweet_str).encode('utf-8')) + 1
        current_position += byte_count
        tweet_str = tweet_file.readline()

# Gather the dict at root process
gather_list = comm.gather([happy_day_dict, happy_hour_dict, active_day_dict, active_hour_dict], root = 0)

happy_hour_dict = {}
happy_day_dict = {}
active_hour_dict = {}
active_day_dict = {}

if rank == 0:
    for list in gather_list:
        happy_day_dict = merge_dict(happy_day_dict, list[0])
        happy_hour_dict = merge_dict(happy_hour_dict, list[1])
        active_day_dict = merge_dict(active_day_dict, list[2])
        active_hour_dict = merge_dict(active_hour_dict, list[3])

    # Final result
    happiest_day, highest_day_sentiment = find_max_value_key(happy_day_dict)
    happiest_hour, highest_hour_sentiment = find_max_value_key(happy_hour_dict)
    most_active_day, most_day_post = find_max_value_key(active_day_dict)
    most_active_hour, most_hour_post = find_max_value_key(active_hour_dict)

    happiest_hour_start = int(happiest_hour[-2:])
    most_active_hour_start = int(most_active_hour[-2:])

    print(f"The happiest day ever, {happiest_day}, was the happiest day with an overall sentiment score of {highest_day_sentiment}.")
    print(f"The happiest hour ever, {happiest_hour[5:10]}, {happiest_hour_start}-{happiest_hour_start+1}, was the happiest hour with an overall sentiment score of {highest_hour_sentiment}.")
    print(f"The most active day ever, {most_active_day}, had the most tweets {most_day_post}.")
    print(f"The most active hour ever, {most_active_hour[5:10]}, {most_active_hour_start}-{most_active_hour_start+1}, had the most tweets {most_hour_post}.")
