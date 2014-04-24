
var API_URL = "../rest/auroraclient";

var tasks = [];
// IndexedDB
var indexedDB = window.indexedDB || window.webkitIndexedDB || window.mozIndexedDB || window.OIndexedDB || window.msIndexedDB,
    IDBTransaction = window.IDBTransaction || window.webkitIDBTransaction || window.OIDBTransaction || window.msIDBTransaction,
    db,
    dbVersion = 1.0;
var processes = [];
var task;
var job;

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
      disk: 256,
      ram: 500,
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
    //callCreateJob();
    callCreateJob(event.target.getAttribute("id"));
    return false;
});


$("#createJobAdvanced").submit(function (event) {
    event.preventDefault();
    //callCreateJob();
    callCreateJob(event.target.getAttribute("id"));
    return false;
});

$('#jobtab a').click(function (e) {
  e.preventDefault();
  //$(this).tab('show');
})

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

var callCreateJob = function (formId) {
    var create_job_url = API_URL + "/createjob";

    var sendData = getSendData(formId);
    if(!sendData.execConfig && sendData.execConfig.length ==0){
        alert('There is no command to process');
    		return;
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
            var link="http://"+sendData.aSchedulerAddr+":8081/scheduler/"+sendData.role+"/"+sendData.environment+"/"+sendData.jobName;
            $("#linkToJob").html("<a href="+link+" target=\"_blank\">"+link+"</a>");
            addJobs(sendData.role,sendData.jobName, sendData.environment, sendData.aSchedulerAddr,sendData.aSchedulerPort,link, JSON.stringify(sendData));
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
            return;
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

function addProcess(ele) {
    var processFragment = $("#processTemplate").html();

    if ($('#processContainer').children().length == 0) {
        $("#processContainer").append(processFragment);
    } else {
        $(ele).parent().after(processFragment);
    }

    $('.btnPRemoveCommand').unbind('click');
    $('.btnPRemoveCommand').click(function () {
        $(this).parent().remove();
        if ($('#processContainer').children().length == 0) {
            $('#btnNewCommand').show();
        }
    });

    $('.btnPAddCommand').unbind('click');
    $('.btnPAddCommand').click(function () {
        addProcess(this);
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
		addProcess(null);
		$('#jobName').focus();
});

$(function () {
  var source = "<div>{{color}}</div>";
  var template = Handlebars.compile(source);
  var context = {
    color: "blue"
  };
  $("#pNext").click(function(e) {
               e.preventDefault();
               getProcesses();
               $('#jobtab li a[href="#task"]').tab("show")
             });
  $("#tNext").click(function(e) {
                 e.preventDefault();
                 getTask();
                 $('#jobtab li a[href="#job"]').tab("show")
               });
  $("#tBack").click(function(e) {
                  e.preventDefault();
                  $('#jobtab li a[href="#process"]').tab("show")
                });
//  $("#jNext").click(function(e) {
//                 e.preventDefault();
//                 $('#jobtab li a[href="#process"]').tab("show")
//               });
  $("#jBack").click(function(e) {
                  e.preventDefault();
                  $('#jobtab li a[href="#task"]').tab("show")
                });

    $( "#pOrderParallel" ).change(function(e) {
        if(!e.target.checked){
            $('.gridlyDiv').show();
        }else{
            $('.gridlyDiv').hide();
        }

    });

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
         var cell5 = row.insertCell(4);
         cell1.innerHTML = res.key;
         cell2.innerHTML = res.value.jobname;
         cell3.innerHTML = "<a href="+res.value.joburl+" target=\"_blank\">"+res.value  .joburl+"</a>";
         cell4.innerHTML = res.value.created;
         cell5.innerHTML = "<span class=\"glyphicon glyphicon-remove\" type=\"button\"></span>";
         $(cell5).data("username",res.value.username);
         $(cell5).data("jobName",res.value.jobname);
         $(cell5).data("scheduler",res.value.scheduler);
         $(cell5).data("port",res.value.port);
         $(cell5).data("environment",res.value.environment);
         $(cell5).addClass("killJob");
         res.continue();
         $( ".killJob" ).unbind();
         $(".killJob").click(function(e){
              var jobName = $(e.target.parentNode).data("jobName");
              BootstrapDialog.confirm('Are you sure you want to kill job : ' + jobName + " ?",function(result){
                if(result) {
                    killJob($(e.target.parentNode).data("scheduler"),$(e.target.parentNode).data("port"),
                    $(e.target.parentNode).data("jobName"),$(e.target.parentNode).data("environment"),$(e.target.parentNode).data("username"));
                }
              });
          });
     }
 }
}

var killJob = function (aSchedulerAddr,aSchedulerPort,jobName,environment,role) {
     var sendData = {
                aSchedulerAddr: aSchedulerAddr,
                aSchedulerPort: aSchedulerPort,
                jobName: jobName,
                environment: environment,
                role: role
            }
    var kill_job_url = API_URL + "/killjob"
    $.ajax({
        url: kill_job_url,
        method: "POST",
        dataType: "json",
        headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
        data:JSON.stringify(sendData),
        contentType:"application/json",
        success: function (data) {
            $("#killJobResult").text(data.status).show().fadeOut(2500);
        },
        error: function (chr, data, error) {
            //attDailyJson = data;
        },
        complete: function (xhr, textStatus) {
            //attDailyJson = textStatus;
        }
    }).done(function (data, textStatus, xhr) {
        if (console && console.log) {
            console.log("Sample of data:", data);
        }
    });
}

BootstrapDialog.confirm = function(message, callback) {
            new BootstrapDialog({
                title: '<h3 class="modal-title">Confirmation</h3>',
                message: '<p class="text-danger">'+message+'</p>',
                closable: false,
                data: {
                    'callback': callback
                },
                buttons: [{
                        label: 'Cancel',
                        action: function(dialog) {
                            typeof dialog.getData('callback') === 'function' && dialog.getData('callback')(false);
                            dialog.close();
                        }
                    }, {
                        label: 'Yes',
                        cssClass: 'btn-primary',
                        action: function(dialog) {
                            typeof dialog.getData('callback') === 'function' && dialog.getData('callback')(true);
                            dialog.close();
                        }
                    }]
            }).open();
        };

function addJobs(username, jobName, environment,scheduler,port, jobUrl, sendData) {


    console.log("About to add "+jobName+":"+jobUrl);

    var transaction = db.transaction(["jobData"],"readwrite");
    var store = transaction.objectStore("jobData");

    //Define a person
    var job = {
        username:username,
        jobname:jobName,
        joburl:jobUrl,
        created:new Date(),
        scheduler:scheduler,
        port:port,
        environment:environment,
        sendData:sendData
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

function getProcesses() {
	var newProcesses = [];

    //    var processObj = {
    //        daemon: false,
    //        name: '',
    //        ephemeral: false,
    //        max_failures: 1,
    //        min_duration: 5,
    //        cmdline: '',
    //        final: false
    //    };

    var pTemplate =$.parseHTML($("#pOrderTemplate").html())[1];
    $("#pgOrder > div").remove();
	$('#processContainer').children('').each(function () {
	    var newPTemplate = pTemplate.cloneNode();
		var newProcess = Object.create(processObj);
		newProcess.name = $(this).find(".pName").val();
		newProcess.cmdline=$(this).find('.pCommand').val();
		newProcess.daemon=$(this).find(".pDaemon").val();
		newProcess.ephemeral=$(this).find(".pEphemeral").val();
		newProcess.max_failures=$(this).find(".pMaxFail").val();
		newProcess.min_duration=$(this).find(".pMinDuration").val();
		newProcess.final=$(this).find(".pFinal").val();
		newPTemplate.innerHTML =  newProcess.name;
		$("#pgOrder").append(newPTemplate);

		// Need to do this 'cos of JS inheritence
		var newProcessStr = JSON.stringify(flatten(newProcess));
		var newProcessObj = eval("("+ newProcessStr  + ")");
		newProcesses.push(newProcessObj);
	});
    $('.gridly').gridly({
        base: 60, // px
        gutter: 20, // px
        columns: 5
      });
    processes =  newProcesses;
	return JSON.stringify(processes);
}

function getTask() {
	var newProcesses = [];


    //        var taskObj = {
    //        		processes: [], //Array of process objects
    //            name: 'process1',
    //            finalization_wait: 30,
    //        		max_failures: 1,
    //            max_concurrency: 0,
    //            resources: {
    //              disk: 134217728,
    //              ram: 134217728,
    //              cpu: 1
    //            },
    //        		constraints: [
    //        			{
    //                order: [] //Array of process names
    //        			}
    //        		]
    //        };
    taskObj.constraints= [{order: [] }];
    if(!$("#pOrderParallel").is(':checked')){
        var elements=$(".gridly > div");
        var order="";
        elements.sort(function(a,b)
        {
            aLeft = $(a).css("left");
            aLeft = aLeft.substr(0,aLeft.length-2);
            bLeft = $(b).css("left");
            bLeft = bLeft.substr(0,bLeft.length-2);
            return aLeft-bLeft ;
        })
        $(elements).each(function (){
            taskObj.constraints[0].order.push($(this).html());
        });
    }else{
        delete taskObj.constraints;
    }
    taskObj.processes=processes;
    taskObj.name=$("#tName").val();
    taskObj.finalization_wait=$("#tFinalWait").val();
    taskObj.max_failures=$("#tMaxFail").val();
    taskObj.max_concurrency=$("#tMaxConcurr").val();
    taskObj.resources.disk=$("#tdisk").val();
    taskObj.resources.ram=$("#tram").val();
    taskObj.resources.cpu=$("#tcpu").val();

    return JSON.stringify(taskObj);
}

function addProcessGrid(){
 var pTemplate =$.parseHTML($("#pOrderTemplate").html())[1];
 var node = $.parseHTML(pTemplate);
}

function getJob() {
	var newProcesses = [];


    //var jobObj = {
    //	priority: 0,
    //	task: new Object(),
    //	name: 'job1',
    //	environment: 'prod',
    //	max_task_failures: 1,
    //	enable_hooks: false,
    //	cluster: 'example',
    //	production: false,
    //	role: 'knagireddy'
    //};

    jobObj.task=taskObj;
    jobObj.name=$("#jName").val();
    jobObj.environment=$("#jEnvironment").val();
    jobObj.max_task_failures=$("#jMaxTFailures").val();
    jobObj.enable_hooks=$("#jEnableHooks").val();
    jobObj.production=$("#jProduction").val();
    jobObj.role=$("#jRole").val();

    return JSON.stringify(jobObj);
}

function getSendData(formId) {
    if(formId=="createJobAdvanced"){
        getJob();
        var sendData = {
                aSchedulerAddr: $("#jASchedulerAddr").val(),
                aSchedulerPort: '8082',
                jobName: jobObj.name,
                environment: jobObj.environment,
                cpu: taskObj.resources.cpu,
                ram: taskObj.resources.ram,
                disk: taskObj.resources.disk,
                execConfig: JSON.stringify(jobObj),
                constraintName: $("#tcname").val(),
                constraintValue: $("#tcvalue").val(),
                role: jobObj.role
            }
    }
    else{
        var sendData = {
            aSchedulerAddr: $("#aSchedulerAddr").val(),
            aSchedulerPort: '8082',
            jobName: $("#jobName").val(),
            environment: $("#environment").val(),
            cpu: $("#cpu").val(),
            ram: $("#ram").val(),
            disk: $("#disk").val(),
            execConfig: getExecConfig(),
            constraintName: $("#cname").val(),
            constraintValue: $("#cvalue").val(),
            role: $("#role").val()
        }
    }

    return sendData;
}
