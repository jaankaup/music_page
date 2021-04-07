"use strict"

const RType = {POST: "post", GET: "get", PUT: "put", DELETE: "delete"};
//const url = "https://jaankaup1.ties478.website/Task3/webapi/";
const debug = true;

/////////////////////////////////////////////////////////////////////////////////////////////////////

function createRequest(requestParameters) {
	
  var parameters = {reqType:RType.GET,
 		    data:{},
 		    service:"",
 		    dataType:"json",
 		    success:defaultSuccess}; 

  // Replace the default parameters with requestParameters.
  for (let p in requestParameters) {
    if (parameters[p] === undefined) {
	    console.debug(`createRequest: '${p}' no such paramer!`);
    }
    else {
	    parameters[p] = requestParameters[p]; 
    }
  }

  // Debugging.
  debug && console.log(`Creating response for ${parameters['service']}.`);
  debug && console.log(`Request type: ${parameters['reqType']}.`);
  debug && console.log(`Data:`);
  debug && console.log(parameters['data']);

  // Latausindikaattori paalle.
  //latausIndikaattoriStart();

  // Tehdaan ajax-kutsu.
  $.ajax({ async: true,
	         url: parameters['service'],
	         type: parameters['reqType'],
       	   data: parameters['data'],
     	     dataType: parameters['dataType'],
     	     //success: parameters['success'],
    	     //error: defaultError
	   }).then(parameters['success'],defaultError);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function defaultSuccess(data, textStatus, request) {
  console.log("Default success:");
  console.log("Data:");
  console.log(data);
  console.log("TextStatus:");
  console.log(textStatus);
  console.log("Request:");
  console.log(request);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function defaultError(request, textStatus, error) {
  console.log("Default error:");
  console.log("Request:");
  console.log(request);
  console.log("TextStatus:");
  console.log(textStatus);
  console.log("Error:");
  console.log(error);
}
