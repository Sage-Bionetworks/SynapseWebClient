'use strict';

exports.handler = (event, context, callback) => {
    const response = event.Records[0].cf.response;
    const headers = response.headers;
    const cacheControlHeader = 'Cache-Control';
    const expiresHeader = 'Expires';
    const pragmaHeader = 'Pragma';
    const noCachePragmaValue = 'no-cache';
    const noCacheValue = 'no-cache, max-age=0, must-revalidate, pre-check=0, post-check=0';
    /**
     * This function overrides the response cache control header value instructing the browser not to cache if
     * the response status code is 4xx or 5xx (cache control header must be set)
     */
    if (response.status >= 400 && response.status <= 599 && headers[cacheControlHeader.toLowerCase()]) {
        headers[cacheControlHeader.toLowerCase()] = [{
            key: cacheControlHeader,
            value: noCacheValue
        }];
        console.log(`Response header "${cacheControlHeader}" was set to ` +
                    `"${headers[cacheControlHeader.toLowerCase()][0].value}"`);
        if (headers[expiresHeader.toLowerCase()]) {
            headers[expiresHeader.toLowerCase()] = [{
                key: expiresHeader,
                value: 0
            }];
            console.log(`Response header "${expiresHeader}" was set to ` +
                    `"${headers[expiresHeader.toLowerCase()][0].value}"`);
        }
        
        if (headers[pragmaHeader.toLowerCase()]) {
            headers[pragmaHeader.toLowerCase()] = [{
                key: pragmaHeader,
                value: noCachePragmaValue
            }];
            console.log(`Response header "${pragmaHeader}" was set to ` +
                    `"${headers[pragmaHeader.toLowerCase()][0].value}"`);
        }
    }
    callback(null, response);
};