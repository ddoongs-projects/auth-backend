FROM nginx:alpine

# Install apache2-utils for htpasswd
RUN apk add --no-cache apache2-utils

# Copy nginx configuration
COPY docker/docs/nginx.conf /etc/nginx/conf.d/default.conf

# Create basic auth password file using build arguments
ARG DOCS_USERNAME=docs
ARG DOCS_PASSWORD=docs123
RUN htpasswd -bc /etc/nginx/.htpasswd ${DOCS_USERNAME} ${DOCS_PASSWORD}

# Copy REST API documentation from build location
COPY support/restdocs/build/docs/asciidoc/ /usr/share/nginx/html/

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]