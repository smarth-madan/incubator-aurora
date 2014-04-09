
var API_URL = "../rest/auroraclient";

var tasks = [];
// IndexedDB
var indexedDB = window.indexedDB || window.webkitIndexedDB || window.mozIndexedDB || window.OIndexedDB || window.msIndexedDB,
    IDBTransaction = window.IDBTransaction || window.webkitIDBTransaction || window.OIDBTransaction || window.msIDBTransaction,
    db,
    dbVersion = 1.0;

var jobObj = {
	priority: 0,
	task: new Object(),
	name: 'job1',
	environment: 'prod',
	max_task_failures: 1,
	enable_hooks: false,
	cluster: 'example',
	production: false,
	role: 'knagireddy'
};

var taskObj = {
		processes: [], //Array of process objects	
    name: 'process1',
    finalization_wait: 30,
		max_failures: 1,
    max_concurrency: 0,
    resources: {
      disk: 134217728,
      ram: 134217728,
      cpu: 1
    },
		constraints: [
			{
        order: [] //Array of process names
			}
		]
};

var processObj = {
	daemon: false,
	name: '',
	ephemeral: false,
	max_failures: 1,
	min_duration: 5,
	cmdline: '',
	final: false
};


$("#createJob").submit(function (event) {
    event.preventDefault();
    daily = false;
    //callCreateJob();
    callCreateJob2();
    return false;
});

function getExecConfig() {
	jobObj.task = taskObj;
	
	var childCount = 1;
	var newProcesses = [];
	$('#commandContainer').children('').each(function () {
		var newProcess = Object.create(processObj);
		newProcess.name = 'process' + childCount++;
		newProcess.cmdline=$(this).find('.txtCommand').val();

		// Need to do this 'cos of JS inheritence
		var newProcessStr = JSON.stringify(flatten(newProcess));
		var newProcessObj = eval("("+ newProcessStr  + ")");
		newProcesses.push(newProcessObj);
	});

	taskObj.processes = newProcesses;
	taskObj.constraints[0].order=[];
	for (var i = 0; i < newProcesses.length; i++) {
		taskObj.constraints[0].order.push(newProcesses[i].name);
	}
	
	return JSON.stringify(jobObj);
}

function flatten(obj) {
    var result = Object.create(obj);
    for(var key in result) {
        result[key] = result[key];
    }
    return result;
}

var callCreateJob = function () {
    var aSchedulerAddr = $("#aSchedulerAddr").val();
//    var aSchedulerPort = $("#aSchedulerPort").val();
		var aSchedulerPort = '8082';
    var jobName = $("#jobName").val();
    var environment = $("#environment").val();
    var cpu = $("#cpu").val();
    var ram = $("#ram").val();
    var disk = $("#disk").val();
    //    var execConfig = $("#execConfig").val();
		var execConfig = getExecConfig();
		if (!execConfig && execConfig.length == 0) {
			alert('There is no command to process');
			return;
		}
    var create_job_url = API_URL + "/createjob?aSchedulerAddr=" + aSchedulerAddr + "&aSchedulerPort=" + aSchedulerPort + "&" +
        "jobName=" + jobName + "&environment=" + environment + "&cpu=" + cpu + "&ram=" + ram + "&disk=" + disk + "&execConfig=" + execConfig;

    $.ajax({
        url: create_job_url,
        method: "POST",
        success: function (data) {
            $("#result").text(data);
            var link="http://"+aSchedulerAddr+":8081/scheduler/knagireddy/"+environment+"/"+jobName;
            $("#linkToJob").html("<a href="+link+" target=\"_blank\">"+link+"</a>");
            addJobs('knagireddy',jobName, link);
            $("#example").modal("toggle");
            var jobTbl = $("#jobtblbody")[0];
            var row = jobTbl.insertRow(-1);
            var cell1 = row.insertCell(0);
            var cell2 = row.insertCell(1);
            var cell3 = row.insertCell(2);
            var cell4 = row.insertCell(3);
            cell2.innerHTML = jobName;
            cell3.innerHTML = "<a href="+link+" target=\"_blank\">"+link+"</a>";
            cell4.innerHTML = new Date();

        },
        error: function (chr, data, error) {
            attDailyJson = data;
        },
        complete: function (xhr, textStatus) {
            attDailyJson = textStatus;
        }
    }).done(function (data, textStatus, xhr) {
        if (console && console.log) {
            console.log("Sample of data:", data);
        }
    });
}

var callCreateJob2 = function () {
    var aSchedulerAddr = $("#aSchedulerAddr").val();
//    var aSchedulerPort = $("#aSchedulerPort").val();
    var aSchedulerPort = '8082';
    var jobName = $("#jobName").val();
    var environment = $("#environment").val();
    var cpu = $("#cpu").val();
    var ram = $("#ram").val();
    var disk = $("#disk").val();
    var cname = $("#cname").val();
    var cvalue = $("#cvalue").val();
    //    var execConfig = $("#execConfig").val();
		var execConfig = getExecConfig();
		if (!execConfig && execConfig.length == 0) {
			alert('There is no command to process');
			return;
		}
    var create_job_url = API_URL + "/createjob2";

    var sendData = {
        aSchedulerAddr: aSchedulerAddr,
        aSchedulerPort: aSchedulerPort,
        jobName: jobName,
        environment: environment,
        cpu: cpu,
        ram: ram,
        disk: disk,
        execConfig: execConfig,
        constraintName: cname,
        constraintValue: cvalue
    }
    $.ajax({
        url: create_job_url,
        method: "POST",
        dataType: "json",
        headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
        data:JSON.stringify(sendData),
        contentType:"application/json",
        success: function (data) {
            $("#result").text(data);
            var link="http://"+aSchedulerAddr+":8081/scheduler/knagireddy/"+environment+"/"+jobName;
            $("#linkToJob").html("<a href="+link+" target=\"_blank\">"+link+"</a>");
            addJobs('knagireddy',jobName, link);
            $("#example").modal("toggle");
            var jobTbl = $("#jobtblbody")[0];
            var row = jobTbl.insertRow(-1);
            var cell1 = row.insertCell(0);
            var cell2 = row.insertCell(1);
            var cell3 = row.insertCell(2);
            var cell4 = row.insertCell(3);
            cell2.innerHTML = jobName;
            cell3.innerHTML = "<a href="+link+" target=\"_blank\">"+link+"</a>";
            cell4.innerHTML = new Date();

        },
        error: function (chr, data, error) {
            attDailyJson = data;
        },
        complete: function (xhr, textStatus) {
            attDailyJson = textStatus;
        }
    }).done(function (data, textStatus, xhr) {
        if (console && console.log) {
            console.log("Sample of data:", data);
        }
    });
}

    function addCommand(ele) {
        var commandFragment = $("#commandTemplate").html();

        if ($('#commandContainer').children().length == 0) {
            $("#commandContainer").append(commandFragment);
        } else {
            $(ele).parent().after(commandFragment);
        }

        $('.btnRemoveCommand').unbind('click');
        $('.btnRemoveCommand').click(function () {
            $(this).parent().remove();
            if ($('#commandContainer').children().length == 0) {
                $('#btnNewCommand').show();
            }
        });

        $('.btnAddCommand').unbind('click');
        $('.btnAddCommand').click(function () {
            addCommand(this);
        });

        $('#btnNewCommand').hide();
				$('#commandContainer').last().find('.txtCommand').focus();
    }

    //All the form filling javascripts.
$(document).ready(function () {

    $('#btnNewCommand').click(function () {
        addCommand(this);
    });
		
		// Empty command
		addCommand(null);
		$('#jobName').focus();
});

$(function () {
  var source = "<div>{{color}}</div>";
  var template = Handlebars.compile(source);
  var context = {
    color: "blue"
  };
});

document.addEventListener("DOMContentLoaded", function(){

    if("indexedDB" in window) {
        idbSupported = true;
    }

    if(idbSupported) {
         var openRequest = indexedDB.open("jobTableDB",1);

         openRequest.onupgradeneeded = function(e) {
            console.log("running onupgradeneeded");
            var thisDB = e.target.result;

            if(!thisDB.objectStoreNames.contains("jobData")) {
                thisDB.createObjectStore("jobData",{ autoIncrement:true });
            }

         }

        openRequest.onsuccess = function(e) {
            console.log("Success!");
            db = e.target.result;
            refreshData();
        }

        openRequest.onerror = function(e) {
            console.log("Error");
            console.dir(e);
        }
    }

},false);

function loadJobs(key){
    var transaction = db.transaction(["jobData"], "readonly");
    var objectStore = transaction.objectStore("jobData");
    var ob = objectStore.get(key);

    ob.onsuccess = function(e) {
        var result = e.target.result;
        console.dir(result);
        if(result) {
            var jobTbl = $("#jobtblbody")[0];
            var row = jobTbl.insertRow(-1);
            var cell1 = row.insertCell(0);
            var cell2 = row.insertCell(1);
            var cell3 = row.insertCell(2);
            cell1.innerHTML = key;
            cell2.innerHTML = result.jobname;
            cell3.innerHTML = "<a href="+result.joburl+" target=\"_blank\">"+result.joburl+"</a>";
        } else {
        }
    }
}

function refreshData(){
  var transaction = db.transaction(["jobData"], "readonly");
  var objectStore = transaction.objectStore("jobData");

 var cursor = objectStore.openCursor();

 cursor.onsuccess = function(e) {
     var res = e.target.result;
     if(res) {
         console.log("Key", res.key);
         console.dir("Data", res.value);
         var jobTbl = $("#jobtblbody")[0];
         var row = jobTbl.insertRow(-1);
         var cell1 = row.insertCell(0);
         var cell2 = row.insertCell(1);
         var cell3 = row.insertCell(2);
         var cell4 = row.insertCell(3);
         cell1.innerHTML = res.key;
         cell2.innerHTML = res.value.jobname;
         cell3.innerHTML = "<a href="+res.value.joburl+" target=\"_blank\">"+res.value  .joburl+"</a>";
         cell4.innerHTML = res.value.created;
         res.continue();
     }
 }

}

function addJobs(username, jobName, jobUrl) {


    console.log("About to add "+jobName+":"+jobUrl);

    var transaction = db.transaction(["jobData"],"readwrite");
    var store = transaction.objectStore("jobData");

    //Define a person
    var job = {
        username:username,
        jobname:jobName,
        joburl:jobUrl,
        created:new Date()
    }

    //Perform the add
    var request = store.add(job);

    request.onerror = function(e) {
        console.log("Error",e.target.error.name);
        //some type of error handler
    }

    request.onsuccess = function(e,job) {
        console.log("Woot! Did it");
    }
}
