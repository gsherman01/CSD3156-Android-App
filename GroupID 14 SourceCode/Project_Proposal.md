+-----------------------------------------------------------------------+
| Project Proposal II -- Cloud Computing                                |
|                                                                       |
| \< **Cloud-Powered GIS Analytics Platform\                            |
| for Urban Decision Support** \>                                       |
|                                                                       |
| Team number: 14                                                       |
|                                                                       |
|   ------------------------------------------------------------------  |
|   Bryan Ang Wei Ze                               2301397              |
|   ---------------------------------------------- -------------------  |
|   Tham Kang Ting                                 2301255              |
|                                                                       |
|   Chew Bangxin Steven                            2303348              |
|                                                                       |
|   Sam Tsang                                      2301552              |
|                                                                       |
|   Sherman Goh Wee Hao                            2301472              |
|   ------------------------------------------------------------------  |
|                                                                       |
| Student names:                                                        |
+-----------------------------------------------------------------------+
| \<Date & Time\>                                                       |
+-----------------------------------------------------------------------+

#  {#section .unnumbered}

# Introduction 

Geospatial data is widely used in urban planning, infrastructure
development, and spatial decision making. Geographic Information Systems
(GIS) allow planners and analysts to study spatial relationships such as
population distribution, accessibility to facilities, and infrastructure
coverage. However, traditional GIS analysis often requires specialized
desktop software and local computational resources, which limits
accessibility and collaboration.

Cloud computing enables GIS tools to be delivered through web-based
applications, where spatial analysis can be performed on cloud
infrastructure and accessed through a browser. This approach allows
users to access GIS services without installing specialized software
while benefiting from scalable and reliable cloud computing resources.

The **goal** of this project is to develop a Cloud-Powered GIS Analytics
Platform that enables users to perform spatial analysis through a
web-based interface. Users will be able to upload spatial datasets or
select predefined datasets and run geospatial analysis tools such as
spatial join, buffer analysis, and proximity analysis. The results will
then be visualized on an interactive map interface.

The **scope** of the project includes designing and implementing a
cloud-based web application that integrates geospatial processing with
cloud infrastructure services. The system will demonstrate the use of
cloud computing to support application scalability, reliability,
elasticity, and security. The project will also explore the use of
prompt-engineered development workflows to accelerate the development of
system components while maintaining a structured system architecture.

# Proposed Design and Components

The proposed system is a **cloud-based web application** that provides
geospatial analysis services through a browser interface. Users will
interact with the application through a web-based map where they can
upload datasets and request spatial analysis functions.

The **overall system architecture** consists of a frontend interface, a
backend processing service, and cloud infrastructure for storage and
compute resources.

The **frontend interface** will provide an interactive map visualization
where users can view spatial layers and submit geospatial analysis
requests. The map interface will be implemented using Leaflet, which
allows GeoJSON data to be visualized dynamically in a browser
environment.

The **backend service** will expose APIs that handle geospatial
processing requests. These APIs will perform spatial computations such
as spatial joins, buffer generation, and proximity analysis using Python
geospatial libraries including GeoPandas and Shapely.

The system will be deployed using cloud infrastructure provided by
Amazon Web Services. Cloud services will provide the computational
environment and infrastructure required for the application.**\
**

### Key Cloud Services {#key-cloud-services .unnumbered}

  -----------------------------------------------------------------------
  **Cloud Service**          **Purpose**
  -------------------------- --------------------------------------------
  **Amazon API Gateway**     Exposes REST APIs for GIS processing

  **AWS Lambda**             Executes geospatial analysis tasks

  **Amazon S3**              Stores spatial datasets

  **Amazon Cognito**         Handles user login and authentication

  **Amazon CloudWatch**      Tracks application logs and usage
  -----------------------------------------------------------------------

The proposed system satisfies the cloud computing requirements of the
project:

  --------------------------------------------------------------------------
  **Requirement**   **Implementation**
  ----------------- --------------------------------------------------------
  **Functional**    Web GIS platform with spatial analysis tools

  **Scalable**      Serverless functions scale automatically based on
                    requests

  **Reliable**      Data stored in cloud object storage with high durability

  **Elastic**       Cloud compute resources scale dynamically

  **Secure**        User authentication and access control
  --------------------------------------------------------------------------

# Schedule

  ------------------------------------------------------------------------
  **Week**   **Milestone**     **Description**
  ---------- ----------------- -------------------------------------------
  Week 11    Project Planning  Define system architecture, cloud services,
             and Design        and overall project scope

  Week       Backend           Implement geospatial analysis APIs and
  11--12     Development       integrate GIS processing libraries

  Week 12    Cloud Deployment  Deploy backend services and storage
                               infrastructure on the cloud platform

  Week       Frontend          Develop interactive web interface and map
  12--13     Development       visualization features

  Week 13    Testing and       Perform system testing and ensure proper
             Integration       integration of components

  Week 13    Final Submission  Prepare project report, demo video, and
                               source code submission
  ------------------------------------------------------------------------

# Team members and roles

The project will be developed by a team of five members.

  ----------------------------------------------------------------------------
  **Member + id**        **Role**                                  **Work%**
  ---------------------- ----------------------------------------- -----------
  Sherman (2301472)\     System architecture, cloud deployment,    50%
  (Leader)               backend integration, GIS analytics        
                         implementation                            

  Member B               Frontend interface design and map         15%
                         visualization                             

  Member C               Dataset preparation and spatial data      15%
                         integration                               

  Member D               Testing and documentation                 10%

  Member E               Demo preparation and user interface       10%
                         refinement                                
  ----------------------------------------------------------------------------

The project leader will manage the system architecture and integration
to ensure consistent implementation and avoid configuration conflicts.

  -----------------------------------------------------------------------------
  **SN**   **Name of\           **Student ID**            **Responsible
           Team Member**                                  Components**
  -------- -------------------- ------------------------- ---------------------
  1                                                       

  2                                                       

  3                                                       

  4                                                       

  5                                                       

  6                                                       

                                                          
  -----------------------------------------------------------------------------
