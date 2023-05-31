<div align="right">
  <img src="https://img.shields.io/badge/-%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-03396c?style=flat-square" alt="简体中文" />
  <a title="en-US" href="README_en-US.md">  <img src="https://img.shields.io/badge/-English-545759?style=flat-squaree" alt="English"></a>
</div>

<img width= "20%" src="https://user-images.githubusercontent.com/46229052/196671093-21ba3438-719d-4dd4-ad79-bfddd1395663.png" align="right" />

# CPNSim

CPNSim：CPNSim是一个面向大规模、跨地域多数据中心场景的算力网络模拟系统。

CPNSim还有一个可视化的项目可以在这里找到：[CPNSim-visualization](https://github.com/slipegg/CPNSim-visualization)。

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

<br></br>
![image](https://user-images.githubusercontent.com/46229052/196704278-4d04778b-1a9e-46da-9ae2-18e6a7a1bae5.png)
<a id="overview"></a>
<img width="20%" src="https://user-images.githubusercontent.com/46229052/196671599-c2c33b14-be0b-4f7a-92b2-533978afb029.png" align="right" />
<p align="justify">
CPNSim面向的是大规模、跨地域多数据中心场景的算力网络场景，它支持的是亲和组任务请求。它除了支持常规的用户请求在数据中心内的调度外，还支持分区状态周期同步，算法运行时间毫秒级刻画，并提供数据中心间调度和数据中心内多调度器并行调度的能力。</p>

CPNSim一方面优化了模拟运行过程中的内存消耗，另一方面优化了模拟运行的效率，使得模拟系统能够支持千万级乃至亿级主机数量和对应万级每秒请求速率的快速模拟。</p>

CPNSim的模拟系统架构如下：

![](https://slipegg-md-pic.oss-cn-shanghai.aliyuncs.com/%E6%A8%A1%E6%8B%9F%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E5%9B%BE.jpg)

模拟场景如下：

![](https://slipegg-md-pic.oss-cn-shanghai.aliyuncs.com/%E6%A8%A1%E6%8B%9F%E5%9C%BA%E6%99%AF.jpg)

<br></br>

![image](https://user-images.githubusercontent.com/46229052/196704803-9a9f53b2-8255-4042-9c16-6c8470489791.png)
<a id="important"></a>

> * <p align="justify">这个项目的开发和维护需要付出相当大的努力。因此，欢迎任何形式的贡献。 </p>
>* <p align="justify">如果你愿意使用这个框架来实现你自己的项目，那么创建一个fork是一个糟糕的解决方案。您不应该修改代码库来实现您的项目，而是通过创建一些子类来扩展它。除非您计划重新分发您的更改，否则您最终会得到一个不兼容且过时的框架版本。该项目不断发展，错误修复是首要任务。您的fork与个人更改将错过这些更新和高性能改进。</p>
>* <p align="justify">如果你刚刚遇到这个项目，并想稍后查看，请不要忘记添加一个星号 :star: :wink:.。</p>
<br></br>
![image](https://user-images.githubusercontent.com/46229052/196704930-a1ef92c7-b62e-42a2-8e82-b6ba8b949070.png)
<a id="exclusive-features"></a>

<p align="justify">
CPNSim提供了很多功能，主要如下。

<img width= "20%" src="https://user-images.githubusercontent.com/46229052/196670148-8f647e7f-ffe2-49ea-865b-2c37ca044cb2.png" align="right" />

- 支持的算力网络场景更加全面，包括：
    - 支持亲和性任务请求的模拟。
    - 支持通过文件自定义亲和组结构，并自动生成有户请求。
    - 支持丰富的网络情况模拟，包括接入延迟，连接延迟，可分配宽带，动态接入延迟。
    - 支持通过文件自定义数据中心间的网络拓扑结构与[CloudsimPlus](https://github.com/cloudsimplus/cloudsimplus)类似。
    - 支持通过文件自定义数据中心情况。
    - 支持数据中心间进行协商或非协商的调度
    - 支持数据中心内多调度器并行调度
    - 支持对多调度器产生的冲突进行处理
    - 支持对数据中心分区
    - 支持对数据中心分区进行状态周期同步
    - ...
- 支持大规模数量主机和请求的模拟
    - 最高模拟536,870,911台主机
    - 模拟万级每秒请求速率

CPNSim与[CloudsimPlus](https://github.com/cloudsimplus/cloudsimplus)的对比如下：

不同数量主机的内存消耗对比：

![](https://slipegg-md-pic.oss-cn-shanghai.aliyuncs.com/CPNSim%E5%92%8CCloudsimPlus%E4%B8%8D%E5%90%8C%E4%B8%BB%E6%9C%BA%E6%95%B0%E9%87%8F%E7%9A%84%E5%86%85%E5%AD%98%E6%B6%88%E8%80%97.png)

不同数量请求的内存消耗对比：

![](https://slipegg-md-pic.oss-cn-shanghai.aliyuncs.com/CPNSim%E5%92%8CCloudsimPlus%E4%B8%8D%E5%90%8C%E8%AF%B7%E6%B1%82%E6%95%B0%E9%87%8F%E7%9A%84%E5%86%85%E5%AD%98%E6%B6%88%E8%80%97.png)

不同数量请求的模拟速度对比：

![](https://slipegg-md-pic.oss-cn-shanghai.aliyuncs.com/CPNSim%E5%92%8CCloudsimPlus%E4%B8%8D%E5%90%8C%E8%AF%B7%E6%B1%82%E6%95%B0%E9%87%8F%E7%9A%84%E6%97%B6%E9%97%B4%E6%B6%88%E8%80%972.png)

![](https://slipegg-md-pic.oss-cn-shanghai.aliyuncs.com/CPNSim%E5%92%8CCloudsimPlus%E4%B8%8D%E5%90%8C%E8%AF%B7%E6%B1%82%E6%95%B0%E9%87%8F%E7%9A%84%E6%97%B6%E9%97%B4%E6%B6%88%E8%80%971.png)

<br></br>