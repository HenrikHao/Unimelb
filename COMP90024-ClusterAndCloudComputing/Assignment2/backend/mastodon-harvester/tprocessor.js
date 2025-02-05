module.exports = async function (context) {
    const postData = context.request.body;

    // Check if the language is English
    if (postData.language === "en") {
        const { id, created_at, content } = postData;

        // Remove HTML tags and emojis from the content
        const cleanContent = content.replace(/<[^>]+>/g, '').replace(/[\u{1F600}-\u{1F64F}]/gu, '');

        // Prepare the data to be returned
        const responseData = {
            id: id,
            createdAt: created_at,
            content: cleanContent
        };

        console.log("Processed data:", JSON.stringify(responseData));

        return {
            status: 200,
            body: JSON.stringify(responseData)
        };
    }
};
