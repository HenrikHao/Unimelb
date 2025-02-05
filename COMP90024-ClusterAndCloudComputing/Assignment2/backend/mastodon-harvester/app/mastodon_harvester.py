"""
COMP90024 Project Team 1
Authors:
- Henrik Hao (1255309)
- Haoyi Li (1237964)
- Zilin Su (1155122)
- Angela Yifei Yuan (1269549)
"""

import json
from mastodon import StreamListener, Mastodon
from aiokafka import AIOKafkaProducer
from datetime import datetime
import asyncio
import os

# Get the API key from the environment variable
api_key = os.getenv("MASTODON_API_KEY")

# Initialize Mastodon API
mastodon = Mastodon(
    access_token=api_key,
    api_base_url='https://aus.social'
)

def default_serializer(obj):
    """JSON serializer for objects not serializable by default json code"""
    if isinstance(obj, datetime):
        return obj.isoformat()  # Convert datetime to ISO 8601 string format
    raise TypeError("Type not serializable")

class KafkaPublisher(StreamListener):
    def __init__(self, producer, topic, loop):
        self.producer = producer
        self.topic = topic
        self.loop = loop  # Pass the loop into the listener

    async def handle_stream_data(self, data):
        """Handles incoming data from Mastodon and publishes to Kafka."""
        print("Received data from Mastodon:", data)
        if data is not None:
            try:
                json_data = json.dumps(data, default=default_serializer)  # Use the custom serializer
                result = await self.producer.send_and_wait(self.topic, json_data.encode('utf-8'))
                print("Data successfully sent to Kafka with offset:", result.offset)

            except Exception as e:
                print("Failed to send data to Kafka:", str(e))

    def on_update(self, status):
        """This method is called when new data is received from the Mastodon stream."""
        # Schedule tasks on the event loop passed to this object
        asyncio.run_coroutine_threadsafe(self.handle_stream_data(status), self.loop)

async def main():
    loop = asyncio.get_running_loop()  # Get the running event loop
    producer = AIOKafkaProducer(bootstrap_servers='my-cluster-kafka-bootstrap.kafka.svc:9092')
    await producer.start()
    
    try:
        listener = KafkaPublisher(producer, "mastodon-topic", loop)  # Pass the loop to the publisher
        stream_handle = mastodon.stream_local(listener, run_async=True)

        while stream_handle.is_alive():
            await asyncio.sleep(10)
    finally:
        await producer.stop()
        stream_handle.close()

if __name__ == '__main__':
    asyncio.run(main())
