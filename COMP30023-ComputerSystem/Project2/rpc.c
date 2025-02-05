#include "rpc.h"
#include <stdlib.h>
#include <stdio.h>
#include <netdb.h>
#include <string.h>
#include <unistd.h>
#include <stdbool.h>
#include <stdint.h>
#include <limits.h>
#include <pthread.h>
#include <endian.h>
#define NONBLOCKING
#define MAX_SIZE 100000
#define MAX_FUNCTIONS 20
int create_listening_socket(char* service);
bool send_request(int sockfd, char *request);
char *recv_response(int sockfd);
bool check_data(rpc_data *data);

// thread data
typedef struct {
    int sockfd;
    rpc_server *srv;
} thread;

struct rpc_server {
    /* Add variable(s) for server state */
    int sockfd;
    rpc_handle* handles[MAX_FUNCTIONS];
};

struct rpc_client {
    /* Add variable(s) for client state */
    int sockfd;
};

struct rpc_handle {
    /* Add variable(s) for handle */
    char name[1001]; // the function name will not exceed 1000
    rpc_handler handler;
};

rpc_server *rpc_init_server(int port) {
    char port_str[6]; // 5 digits for a port number, one digit for \0
    sprintf(port_str, "%d", port);

    // malloc a rpc_server
    rpc_server *srv = malloc(sizeof(rpc_server));
    if (srv == NULL) {
        fprintf(stderr, "Failed to allocate memory for rpc_server\n");
        return NULL;
    }
    srv->sockfd = create_listening_socket(port_str);
    if (srv->sockfd < 0) {
        free(srv);
        return NULL;
    }
    // Initialize all handles to NULL
    for (int i = 0; i < MAX_FUNCTIONS; i++) {
        srv->handles[i] = NULL; 
    }
    return srv;
}

int rpc_register(rpc_server *srv, char *name, rpc_handler handler) {
    if (srv == NULL || name == NULL || handler == NULL) {
        return -1;
    }

    // Check if name is valid
    for (int i = 0; name[i] != '\0'; ++i) {
        if (name[i] < 32 || name[i] > 126) {
            fprintf(stderr, "Invalid character in function name\n");
            return -1;
        }
    }

    int namelen = strlen(name);
    if (namelen == 0 || namelen > 1000) {
        return -1;
    }

    for (int i = 0; i < MAX_FUNCTIONS; i++) {
        if (srv->handles[i] == NULL) {
            // Allocate memory for a new rpc_handle
            rpc_handle* new_handle = malloc(sizeof(rpc_handle));
            // Set the name and handler
            strcpy(new_handle->name, name);
            new_handle->handler = handler;
            // Add the new handle to the server
            srv->handles[i] = new_handle;
            return i;
        }
        else {
            if (strcmp(srv->handles[i]->name, name) == 0) {
                srv->handles[i]->handler = handler;
                return i;
            }
        }
    }
    
    return -1;
}

void* handle_client(void* data) {
    thread *t = (thread *)data;
    int new_sockfd = t->sockfd;
    rpc_server *srv = t->srv;

    while (1) {
        char buffer[1024];

        int len;
        if (recv(new_sockfd, &len, sizeof(len), 0) != sizeof(int)) {
            break;
        }
        if (recv(new_sockfd, buffer, len, 0) != len) {
            perror("Receiving failed");
            break;
        }
        buffer[len - 1] = '\0';

        char* saveptr;
        char* request_type = strtok_r(buffer, "|", &saveptr);
        char* function_name = strtok_r(NULL, "|", &saveptr);

        // remove trailing newline from function_name if it exists
        size_t fn_len = strlen(function_name);
        if(function_name[fn_len-1] == '\n') {
            function_name[fn_len-1] = '\0';
        }

        // Handle rpc_find
        if (strcmp(request_type, "FIND") == 0) {
            bool found = false;
            // Handle a FIND request
            for (int i=0; i<MAX_FUNCTIONS; i++) {
                if (srv->handles[i] != NULL) {
                    //printf("Comparing %s and %s\n", srv->handles[i]->name, function_name);

                    if (strcmp(srv->handles[i]->name, function_name) == 0) {
                        //printf("3%s", function_name);
                        found = true;
                    }
                }
            }
            if (found) {
                // The function was found. Send an OK response.
                char response[1024];
                sprintf(response, "OK|%s\n", function_name);
                send(new_sockfd, response, strlen(response), 0);
            } else {
                // The function was not found. Send an ERROR response.
                char* response = "ERROR|Function not found\n";
                send(new_sockfd, response, strlen(response), 0);
            }
        }

        if (strcmp(request_type, "CALL") == 0) {
            rpc_data args;

            uint64_t recv_data;
            len = recv(new_sockfd, &(recv_data), sizeof(uint64_t), 0);
            if (len != sizeof(uint64_t)) {
                perror("Receiving data 1 failed");
                break;
            }
            args.data1 = (int)(be64toh(recv_data));

            uint64_t recv_length;
            len = recv(new_sockfd, &(recv_length), sizeof(uint64_t), 0);
            if (len != sizeof(uint64_t)) {
                perror("Receiving data2_length failed");
                break;
            }
            args.data2_len = (size_t)(be64toh(recv_length));
            
            if (args.data2_len > 0){
                    args.data2 = malloc(args.data2_len);
                    len = recv(new_sockfd, args.data2, args.data2_len, 0);
                    if (len != args.data2_len) {
                        perror("Data2 failed");
                        break;
                    }
                }
            

            // find the function
            rpc_handle* h = NULL;
            for (int i=0; i<MAX_FUNCTIONS; i++) {
                if (srv->handles[i] != NULL && strcmp(srv->handles[i]->name, function_name) == 0) {
                    h = srv->handles[i];
                    break;
                }
            }
            if (h != NULL) {
                // call the function and get result
                rpc_data *result_data = h->handler(&args);

                int valid = 0;
                if (check_data(result_data)) {
                    len = snprintf(buffer, sizeof(buffer), "RESULT|\n");
                    valid = len;
                    send(new_sockfd, &valid, sizeof(int), 0);
                } else {
                    send(new_sockfd, &valid, sizeof(int), 0);
                    
                    continue;
                }

                int64_t long_data = (int64_t)result_data->data1;

                if (long_data > INT_MAX || long_data < INT_MIN) {
                    perror("data1 not in range");
                    break;
                }

                uint64_t send_data = htobe64(long_data);
                if (send(new_sockfd, &send_data, sizeof(uint64_t), 0) != sizeof(uint64_t)) {
                    perror("Send data1 to client failed");
                    break;
                }

                uint64_t send_length = htobe64((uint64_t)result_data->data2_len);
                if (send(new_sockfd, &send_length, sizeof(uint64_t), 0) != sizeof(uint64_t)) {
                    perror("Send data2_length to client failed");
                    break;
                }

                if (send(new_sockfd, result_data->data2, result_data->data2_len, 0) != result_data->data2_len) {
                    perror("Send data2 to client failed");
                    break;
                }

                // free the result_data
                rpc_data_free(result_data);
                
                
            } else {
                int valid = 0;
                // Send length of error message
                if (send(new_sockfd, &valid, sizeof(int), 0) != sizeof(int)) {
                    perror("Sending error message length failed");
                    break;
                }
                
            }
        }
    }

    close(new_sockfd);
    return NULL;
}


void rpc_serve_all(rpc_server *srv) {
    struct sockaddr_in6 client_addr;
    socklen_t client_addr_size = sizeof(client_addr);

    while (1) {
        // accept a connection
        int new_sockfd = accept(srv->sockfd, (struct sockaddr*)&client_addr, &client_addr_size);
        // check connection
        if (new_sockfd < 0) {
            perror("accept");
            break;
        }

        thread *t = malloc(sizeof(thread));
		t->sockfd = new_sockfd;
		t->srv = srv;
        pthread_t thread_id;
        if (pthread_create(&thread_id, NULL, handle_client, t)) {
            perror("Failed to create thread");
            continue;
        }
        pthread_detach(thread_id);
    }
}

rpc_client *rpc_init_client(char *addr, int port) {
    int s;
    struct addrinfo hints, *res;
    rpc_client *client;

    client = malloc(sizeof(rpc_client));
    if (client == NULL) {
        perror("malloc");
        return NULL;
    }

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_INET6;     // IPv6
    hints.ai_socktype = SOCK_STREAM; // Connection-mode byte streams

    // Convert port number from integer to string
    char port_str[6];
    sprintf(port_str, "%d", port);

    s = getaddrinfo(addr, port_str, &hints, &res);
    if (s != 0) {
        fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(s));
        free(client);
        return NULL;
    }

    client->sockfd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if (client->sockfd < 0) {
        perror("socket");
        freeaddrinfo(res);
        free(client);
        return NULL;
    }

    if (connect(client->sockfd, res->ai_addr, res->ai_addrlen) < 0) {
        perror("connect");
        freeaddrinfo(res);
        close(client->sockfd);
        free(client);
        return NULL;
    }

    freeaddrinfo(res);

    return client;
}

rpc_handle *rpc_find(rpc_client *cl, char *name) {
    if (cl == NULL || name == NULL) {
        perror("rpc_find");
        return NULL;
    }
    int name_len = strlen(name);
    if (name_len == 0 || name_len > 1000) {
        perror("invalid length");
        return NULL;
    }

    char request[1024];
    int len = snprintf(request, sizeof(request), "FIND|%s\n", name);

    if (request == NULL) {
        perror("Request Empty");
        return NULL;
    }

    if (send(cl->sockfd, &len, sizeof(int), 0) != sizeof(int)) {
        perror("Length sending failed");
        return NULL;
    }

    if (send(cl->sockfd, request, len, 0) != len) {
        perror("Request Sending failed");
        return NULL;
    }

    // recv response from the server
    char* response = recv_response(cl->sockfd);
    if (response == NULL) {
        fprintf(stderr, "Failed to receive response from server\n");
        return NULL;
    }

    // parse the response
    if (strncmp(response, "OK|", 3) == 0) {
        // Function was found, return an rpc_handle for it
        rpc_handle *handle = malloc(sizeof(rpc_handle));
        strcpy(handle->name, name);
        handle->handler = NULL; // We don't know the handler on the client side
        return handle;
    }
    return NULL;
    
}

rpc_data *rpc_call(rpc_client *cl, rpc_handle *h, rpc_data *payload) {
    if (cl == NULL || h == NULL || payload == NULL) {
        perror("basic check");
        return NULL;
    }

    if (!check_data(payload)) {
        perror("Bad Client");
        return NULL;
    }

    // Check if data2_len is too large to fit into the request
    if (payload->data2_len > MAX_SIZE) {
        perror("data exceeded");
        return NULL;
    }

    char request[1024];
    int len = snprintf(request, sizeof(request), "CALL|%s\n", h->name);
    
    if (request == NULL) {
        perror("Request Empty");
        return NULL;
    }

    if (send(cl->sockfd, &len, sizeof(int), 0) != sizeof(int)) {
        perror("rpc_call: Length sending failed");
        return NULL;
    }

    if (send(cl->sockfd, request, len, 0) != len) {
        perror("rpc_call: Request Sending failed");
        return NULL;
    }
    
    int64_t long_data = payload->data1;
    if (long_data > INT_MAX || long_data < INT_MIN) {
        perror("Data not in range");
        return NULL;
    }
    uint64_t send_data = htobe64(long_data);

    if (send(cl->sockfd, &send_data, sizeof(uint64_t), 0) != sizeof(uint64_t)) {
        perror("rpc_call: Send data1 failed");
        return NULL;
    }

    uint64_t send_length = htobe64(payload->data2_len);
    
    if (send(cl->sockfd, &send_length, sizeof(uint64_t), 0) != sizeof(uint64_t)) {
        perror("rpc_call: Send data2_length failed");
        return NULL;
    }

    if (send(cl->sockfd, payload->data2, payload->data2_len, 0) != payload->data2_len) {
        perror("rpc_call: Send data2 failed");
        return NULL;
    }

    // recv response from the server
    int valid;
    len = recv(cl->sockfd, &valid, sizeof(int), 0);

    // check if message is valid
    if (len <= 0) {
        perror("Receiving result failed");
        return NULL;
    }

    if (!valid) {
        perror("result not valid");
        return NULL;
    }

    // malloc the rpc_data
    rpc_data* result = malloc(sizeof(rpc_data));
    if (!result) {
        perror("malloc");
        return NULL;
    }

    uint64_t recv_data;
    len = recv(cl->sockfd, &(recv_data), sizeof(uint64_t), 0);
    if (len != sizeof(uint64_t)) {
        return NULL;
    }
    result->data1 = (int)(be64toh(recv_data));

    uint64_t recv_length;
    len = recv(cl->sockfd, &(recv_length), sizeof(uint64_t), 0);
    if (len != sizeof(uint64_t)) {
        return NULL;
    }
    result->data2_len =  (size_t)(be64toh(recv_length));

    if (result->data2_len >= 100000) {
        fprintf(stderr, "Overlength error");
        return NULL;
    }
    if (result->data2_len < 0) {
        return NULL;
    }

    result->data2 = malloc(result->data2_len); 
    if (!result->data2) {
        perror("malloc");
        free(result);
        return NULL;
    }

    if (result->data2_len > 0) {
        len = recv(cl->sockfd, result->data2, result->data2_len, 0);
        if (len != result->data2_len) {
            perror("data2 failed");
            return NULL;
        }
    } else {
        result->data2 = NULL;
    }

    return result;
}


void rpc_close_client(rpc_client *cl) {
    free(cl);
}

void rpc_data_free(rpc_data *data) {
    if (data == NULL) {
        return;
    }
    if (data->data2 != NULL) {
        free(data->data2);
    }
    free(data);
}

// check if data is valid for bad_server and bad_client
bool check_data(rpc_data *data) {
    if (data == NULL) {
        return false;
    }

    if (data->data2_len == 0 && data->data2 != NULL) {
        return false;
    }

    if (data->data2_len !=0 && data->data2 == NULL) {
        return false;
    }
    return true;
}

int create_listening_socket(char* service) {
    int re, s, sockfd;
    struct addrinfo hints, *res;

    // Create address we're going to listen on (with given port number)
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_INET6;      // IPv6
    hints.ai_socktype = SOCK_STREAM; // Connection-mode byte streams
    hints.ai_flags = AI_PASSIVE;     // for bind, listen, accept
    // node (NULL means any interface), service (port), hints, res
    s = getaddrinfo(NULL, service, &hints, &res);
    if (s != 0) {
        fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(s));
        exit(EXIT_FAILURE);
    }

    // Create socket
    sockfd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if (sockfd < 0) {
        perror("socket");
        exit(EXIT_FAILURE);
    }

    // Reuse port if possible
    re = 1;
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &re, sizeof(int)) < 0) {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }
    // Bind address to the socket
    if (bind(sockfd, res->ai_addr, res->ai_addrlen) < 0) {
        perror("bind");
        exit(EXIT_FAILURE);
    }

    if (listen(sockfd, 5) < 0) {
        perror("listen");
        exit(EXIT_FAILURE);
    }
    freeaddrinfo(res);

    return sockfd;
}

bool send_request(int sockfd, char *request) {
    if (request == NULL) {
        return false;
    }
    int len = strlen(request);
    int n = send(sockfd, request, len, 0);
    if (n < 0) {
        perror("send_request");
        return false;
    }
    
    return true;
}

char *recv_response(int sockfd) {
    char buffer[1024];
    memset(buffer, 0, sizeof(buffer));

    int n = recv(sockfd, buffer, sizeof(buffer) - 1, 0);
    if (n < 0) {
        perror("receive_response_from_server");
        return NULL;
    }

    buffer[n] = '\0';  // ensure the received string is null-terminated

    char *response = strdup(buffer);
    if (response == NULL) {
        fprintf(stderr, "Failed to allocate memory for response\n");
        return NULL;
    }

    return response;
}

