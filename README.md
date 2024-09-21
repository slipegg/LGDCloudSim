<a id="top"></a>

<p align="center">
<b><a href="#overview">Overview</a></b>
|
<b><a href="#scenario">Scenario</a></b>
|
<b><a href="#feature">Exclusive Features</a></b>
|
<b><a href="#design">System Design</a></b>
|
<b><a href="#usage">How to use</a></b>
|
<b><a href="#example">Example</a></b>
|
<b><a href="#docs-help">Docs and Help</a></b>
|
<b><a href="#projects">Related Projects</a></b>
|
<b><a href="#license">License</a></b>
</p>

<a id="overview"></a>

# 1. Overview

**LGDCloudSim** is a resource management simulation system for **large-scale geographically distributed cloud data center scenarios**.
It support simulate diverse scheduling architectures and different request types in large-scale scenarios.
The programming language it uses is Java17.
The resource allocation model it simulates mainly includes two layers: host and instance. Instances occupy host resources. Tasks have a fixed life cycle and resources are released directly after expiration.
Note that LGDCloudSim does not simulate the actual running process of the instance, but only focuses on simulating diverse scheduling strategies.

LGDCloudSim has made a lot of efforts to support the simulation of larger-scale scenarios, including **state management optimization** (unifying hosts as int arrays and using  a multi-level incremental state representation method to save historical sync resource states) and **operation process optimization** (trying to aggregate the same type of events that occur at the same time into one event for processing together).
After testing on a 16-core 32G server, LGDCloudSim can support a total number of hosts reaching **500 million** and concurrent requests reaching **10 million**.
More details will be available in the code and in a future paper.

> **Note**
> 
> If you are using CloudSim Plus in your research, please make sure you cite this paper: Liu J, Xu Y, Feng B, et al. [LGDCloudSim: A resource management simulation system for large-scale geographically distributed cloud data center scenarios](https://ieeexplore.ieee.org/abstract/document/10643915)[C]//2024 IEEE 17th International Conference on Cloud Computing (CLOUD). IEEE, 2024: 194-204.
>
> **In addinition**
> 
> We are honored that this paper won the Best Student Paper Award at [IEEE CLOUD 2024](https://cloud.conferences.computer.org/2024/).

## Important

* The development and maintenance of this project requires a considerable effort. Thus, any form of contribution is encouraged. We would be very grateful if you could give it a star ⭐ using the button at the top of the GitHub page👏.

* If you are willing to use the system to implement your own project on top of it, creating a fork is a bad solution. You are not supposed to modify the code base to implement your project, but extend it by creating some subclasses. Unless you plan to redistribute your changes, you will end up with an incompatible and obsolete version of the system. The project is constantly evolving and bug fixes are a priority. Your fork with personal changes will miss these updates and high performance improvements.

<a id="scenario"></a>

# 2. Scenario

The scenario simulated by LGDCloudSim is shown in the Fig.1.

![Fig.1 scenario](https://github.com/user-attachments/assets/6a86e1c7-edce-4f4d-bc40-41ec4325978f)

Data centers are distributed across various regions. 
Mutually trusted data centers, such as those from the same IaaS provider, establish a collaborative zone for user request forwarding. 
In Fig.1, data centers with the same color form a collaborative zone. 
Each collaboration zone has an upper-level cloud administrator to manage its data centers.
It records basic information about the data center, handles failed requests and maintains a centralized inter-scheduler if one exists.
Each data center houses numerous hosts, and the resource types of each host include CPU, memory, storage, and bandwidth.
The capacity of each resource type may vary among different hosts.

Users are dispersed in different areas worldwide and send user requests through the network.
The network connects users, data centers, and cloud administrators.
The basic condition of each network connection is represented by static network latency.
Real-time changing information delivery latency is represented by dynamic network latency.
There are also allocatable bandwidth resources between data centers.
The user request structure is shown in Fig.1.
Each user request consists of multiple instance groups, each containing several instances. 
If any instance schedule fails, the entire request is considered a failure.
Each instance needs to run on a host. It must specify its running lifecycle and required resources like CPU, memory, storage, and bandwidth. 
Each instance within the same group may vary but must run in the same data center.
If any instance groups in a user request have access latency constraints, or if there are connection latency constraints or bandwidth needs between any instance groups, then this user request is called an affinity request; 
otherwise, it is called an ordinary request if there are no network constraints.

Depending on the SA, user requests can be sent to the cloud administrator or the data center.
The cloud administrator's inter-scheduler can either directly schedule instances of user requests to hosts in various data centers or simply forward them to each data center by the simple state of every data center.
The inter-scheduler in the data center can forward user requests to other data centers or schedule them to hosts. 
When user requests are only scheduled within the data center, multiple intra-schedulers can be used for scheduling. 
The load balancer distributes user requests to each intra-scheduler, and each intra-scheduler then schedules user requests to hosts. 
Each inter-scheduler and intra-scheduler obtains the states of the host through synchronization.
In addition, to address potential conflicts, each scheduling result must pass through a conflict handler before being placed to run on a host.

<p align="right"><a href="#top">:arrow_up:</a></p>

<a id="feature"></a>

# 3. Exclusive Features

LGDCloudSim offers many exclusive features, many of which are generated by large-scale scenarios:

## 3.1. Resource

* Support host state partition synchronization from master's state manager to the intra-scheduler.
* Support host status updating through heartbeat.
* Support generating and synchronizing customized simple host states to the inter-scheduler.
* Support prediction based on historical host status obtained through synchronization.

## 3.2 Network

* Support simulating network delays between data centers in units of regions.
* Support simulating dynamic network delays between data centers through dynamic network fluctuation models.
* Support simulating bandwidth between data centers.
* Support users to use areas as units and data centers to use regions as units to simulate the access delay between users and data centers.
* Provides network data processed from the real [Google network dataset](https://cloud.google.com/network-intelligence-center/docs/performance-dashboard/how-to/view-google-cloud-latency).

## 3.3 Request

* Support ordinary requests.
* Support affinity requests with access latency constraints or connection latency constraints or bandwidth needs.

## 3.4 Scheduler

* Support diverse active request forwarding by upper-layer centralized inter-scheduler or data center's inter-scheduler.
* Support parallel scheduling of multiple intra-schedulers in the data center.
* Support dividing intra-scheduler's scheduling view.
* Support load balancing of intra-schedulers and inter-scheduler.
* Support handling of scheduling conflicts.
* Support tracking scheduling time.

## 3.5 Others

* Support large-scale simulation up to 500 million host and 10 million request in a 16-core 32G server.
* Supports diversified log output for the simulation process (through components of [CloudSim Plus](https://github.com/cloudsimplus/cloudsimplus)).
* Support using SQLite database to record scheduling simulation results.
* Support scene setting through files or codes.
* Support generating data center resources and user requests relying on [Google Cluster datasets](https://github.com/google/cluster-data) and [Alibaba cluster dataset](https://github.com/alibaba/clusterdata/tree/master/cluster-trace-v2018)
* ...

<p align="right"><a href="#top">:arrow_up:</a></p>

<a id="design"></a>

# 4. System Design

Fig. 2 shows the architectural elements and multi-layered design of the LGDCloudSim. Its fundamental components consist of four modules: simulation core module, network module, data center module, and user request module. 
System users can customize data centers, requests, and networks tobuild different scenarios and  define execution strategies for testing on the provided platform.

![Fig.2 system design](https://github.com/slipegg/LGDCloudSim/assets/65942634/dc716f0d-7230-437f-816f-ade959e4ac96)

<p align="right"><a href="#top">:arrow_up:</a></p>

<a id="usage"></a>

# 5. How to use LGDCloudSim

LGDCloudSim is a Java 17 project that uses maven for build and dependency management. 
To build and run the project, you need JDK 17+ installed and an updated version of maven (such as 3.8.6+). 
Maven is already installed on your IDE. 
Unless it's out-of-date or you want to build the project from the command line, you need to install maven into your operating system. 
All project dependencies are download automatically by maven.

We highly recommend you use [IDEA](https://www.jetbrains.com/idea/) to use LGDCloudSim.
IDEA can quickly install the environment required for the project, allowing you to focus on running and modifying LGDCloudSim, if necessary.
You can first try to run the examples in IDEA, they are under the src/main/java/org/example folder.

<p align="right"><a href="#top">:arrow_up:</a></p>

<a id="example"></a>

# 6. A simple example

A simple running example is shown below. 
There is no need to configure the scene too much in the code, but only needs to customize it through files.

```java
    private SimpleExample() {
        Log.setLevel(Level.INFO);
        LGDCloudSim = new CloudSim();
        factory = new FactorySimple();
        initUser();
        initDatacenters();
        initNetwork();
        LGDCloudSim.start();
    }

    private void initUser() {
        userRequestManager = new UserRequestManagerCsv(USER_REQUEST_FILE);
        user = new UserSimple(LGDCloudSim, userRequestManager);
    }

    private void initDatacenters() {
        InitDatacenter.initDatacenters(LGDCloudSim, factory, DATACENTER_CONFIG_FILE);
    }

    private void initNetwork() {
        NetworkTopology networkTopology = new NetworkTopologySimple(REGION_DELAY_FILE, AREA_DELAY_FILE, DATACENTER_BW_FILE);
        networkTopology.setDelayDynamicModel(new RandomDelayDynamicModel());
        LGDCloudSim.setNetworkTopology(networkTopology);
    }
```
<a id="docs-help"></a>
# 7. Documentation and Help

The specific documentation for each class in LGDCloudSim has been updated and can be obtained online through [ReadTheDocs](#). I hope it will be helpful to you. 
In addition, if you have any questions, you can also ask them through the issue on github, and I will be happy to help you as much as I can.

<p align="right"><a href="#top">:arrow_up:</a></p>

<a id="projects"></a>

# 8. Related Projects

The simulation event structure, event queues, and event-driven rules are based on [CloudSim 7G](https://github.com/Cloudslab/cloudsim), while the logger is derived from [CloudSim Plus](https://github.com/cloudsimplus/cloudsimplu).
Thanks very much for the great work they did.

<p align="right"><a href="#top">:arrow_up:</a></p>

<a id="license"></a>

# 9. License

This project is licensed under [GNU GPLv3](http://www.gnu.org/licenses/gpl-3.0), as defined inside source files of CloudSim 7G and CloudSim Plus .

<p align="right"><a href="#top">:arrow_up:</a></p>
