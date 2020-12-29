--
-- Table structure for table `Classrooms`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Classrooms` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Contacts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Contacts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `email` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `mobile_phone` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `job` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `student1` int(11) DEFAULT NULL,
  `student2` int(11) DEFAULT NULL,
  `student3` int(11) DEFAULT NULL,
  `student4` int(11) DEFAULT NULL,
  `student5` int(11) DEFAULT NULL,
  `notes` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Contacts_1_idx` (`student1`),
  KEY `fk_Contacts_2_idx` (`student2`),
  KEY `fk_Contacts_3_idx` (`student3`),
  KEY `fk_Contacts_4_idx` (`student4`),
  CONSTRAINT `fk_Contacts_1` FOREIGN KEY (`student1`) REFERENCES `Students` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Contacts_2` FOREIGN KEY (`student2`) REFERENCES `Students` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Contacts_3` FOREIGN KEY (`student3`) REFERENCES `Students` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Contacts_4` FOREIGN KEY (`student4`) REFERENCES `Students` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=253 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeletedEvents`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeletedEvents` (
  `id` int(11) NOT NULL,
  `oldid` int(11) DEFAULT NULL,
  `deletiondate` date DEFAULT NULL,
  `date` date DEFAULT NULL,
  `student` int(11) DEFAULT NULL,
  `event_type` int(11) DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `event_sub` int(11) DEFAULT NULL,
  `notes` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  `teacher` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DriveFolderKeys`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DriveFolderKeys` (
  `folder` varchar(45) COLLATE utf8_spanish_ci NOT NULL,
  `secret` varchar(45) COLLATE utf8_spanish_ci NOT NULL,
  PRIMARY KEY (`folder`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Events`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `student` int(11) DEFAULT NULL,
  `event_type` int(11) DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `event_sub` int(11) DEFAULT NULL,
  `notes` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  `teacher` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Event_1_idx` (`student`),
  KEY `fk_Event_2_idx` (`event_type`),
  KEY `fk_Events_1_idx` (`teacher`),
  CONSTRAINT `fk_Event_1` FOREIGN KEY (`student`) REFERENCES `Students` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Event_2` FOREIGN KEY (`event_type`) REFERENCES `Events_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_1` FOREIGN KEY (`teacher`) REFERENCES `Teachers` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=164639 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventsEoYReport`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventsEoYReport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `student` int(11) DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `notes` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  `teacher` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9788 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventsYet`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventsYet` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `student` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `notes` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  `teacher` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2678 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Events_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Events_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Global_vars`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Global_vars` (
  `year` int(11) NOT NULL,
  `name` varchar(16) COLLATE utf8_spanish_ci NOT NULL,
  `value` varchar(32) COLLATE utf8_spanish_ci NOT NULL,
  PRIMARY KEY (`year`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Links`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Links` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `presentation` int(11) NOT NULL,
  `presentation_sub` int(11) DEFAULT NULL,
  `Outcomes` int(11) DEFAULT NULL,
  `Targets` int(11) DEFAULT NULL,
  `Comment` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=403 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Media`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Media` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `student` int(11) DEFAULT NULL,
  `presentation` int(11) DEFAULT NULL,
  `presentation_sub` int(11) DEFAULT NULL,
  `comment` mediumtext COLLATE utf8_spanish_ci DEFAULT NULL,
  `fileId` varchar(120) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=797 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NC_areas`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NC_areas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `nombre` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `NC_subareas`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NC_subareas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(120) COLLATE utf8_spanish_ci DEFAULT NULL,
  `nombre` varchar(120) COLLATE utf8_spanish_ci DEFAULT NULL,
  `area` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_NC_subareas_1_idx` (`area`),
  CONSTRAINT `fk_NC_subareas_1` FOREIGN KEY (`area`) REFERENCES `NC_areas` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=164 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Observations`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Observations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Outcomes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Outcomes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8_spanish_ci DEFAULT NULL,
  `nombre` varchar(256) COLLATE utf8_spanish_ci DEFAULT NULL,
  `subarea` int(11) DEFAULT NULL,
  `start_month` int(11) DEFAULT NULL,
  `end_month` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Outcomes_1_idx` (`subarea`)
) ENGINE=MyISAM AUTO_INCREMENT=435 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Presentations`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Presentations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(120) COLLATE utf8_spanish_ci DEFAULT NULL,
  `subarea` int(11) DEFAULT NULL,
  `year` double DEFAULT NULL,
  `year_end` double DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  `nombre` varchar(120) CHARACTER SET utf8 DEFAULT NULL,
  `description` longtext CHARACTER SET utf8 DEFAULT NULL,
  `nc1` int(11) DEFAULT NULL,
  `nc2` int(11) DEFAULT NULL,
  `nc3` int(11) DEFAULT NULL,
  `nc4` int(11) DEFAULT NULL,
  `nc5` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Presentations_1_idx` (`subarea`),
  CONSTRAINT `fk_Presentations_1` FOREIGN KEY (`subarea`) REFERENCES `Presentations_subareas` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2381 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Presentations_areas`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Presentations_areas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `nombre` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Presentations_indirect_targets`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Presentations_indirect_targets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `presentation` int(11) DEFAULT NULL,
  `targets` varchar(60) COLLATE utf8_spanish_ci DEFAULT NULL,
  `outcomes` varchar(60) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Presentations_Direct_Targets_1_idx` (`presentation`),
  KEY `fk_Presentations_Direct_Targets_2_idx` (`targets`),
  CONSTRAINT `fk_Presentations_Direct_Targets_1` FOREIGN KEY (`presentation`) REFERENCES `Presentations` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Presentations_sub`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Presentations_sub` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  `nombre` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  `presentation` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Presentations_sub_1_idx` (`presentation`),
  CONSTRAINT `fk_Presentations_sub_1` FOREIGN KEY (`presentation`) REFERENCES `Presentations` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1655 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Presentations_subareas`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Presentations_subareas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(90) COLLATE utf8_spanish_ci DEFAULT NULL,
  `area` int(11) DEFAULT NULL,
  `nombre` varchar(90) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Subarea_1_idx` (`area`),
  CONSTRAINT `fk_Subarea_1` FOREIGN KEY (`area`) REFERENCES `Presentations_areas` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=137 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Students`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Students` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `classroom` int(11) DEFAULT NULL,
  `address` varchar(200) COLLATE utf8_spanish_ci DEFAULT NULL,
  `chronic diseases` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `medical treatment` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `allergies or dietary restrictions` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `special needs` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `taking medications` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `firstday_snails` date DEFAULT NULL,
  `firstday_cdb` date DEFAULT NULL,
  `firstday_primary` date DEFAULT NULL,
  `exit_date` date DEFAULT NULL,
  `notes` longtext COLLATE utf8_spanish_ci DEFAULT NULL,
  `drive_main` varchar(64) COLLATE utf8_spanish_ci DEFAULT NULL,
  `drive_documents` varchar(64) COLLATE utf8_spanish_ci DEFAULT NULL,
  `drive_photos` varchar(64) COLLATE utf8_spanish_ci DEFAULT NULL,
  `drive_reports` varchar(64) COLLATE utf8_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Students_1_idx` (`classroom`),
  CONSTRAINT `fk_Students_1` FOREIGN KEY (`classroom`) REFERENCES `Classrooms` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=169 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Targets`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Targets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(512) COLLATE utf8_spanish_ci DEFAULT NULL,
  `nombre` varchar(512) COLLATE utf8_spanish_ci DEFAULT NULL,
  `NC` tinyint(1) DEFAULT NULL,
  `subarea` int(11) DEFAULT NULL,
  `year` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Targets_1_idx` (`subarea`),
  CONSTRAINT `fk_Targets_1` FOREIGN KEY (`subarea`) REFERENCES `NC_subareas` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1323 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Targets_Montessori`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Targets_Montessori` (
  `id` int(11) NOT NULL,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `age` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Teachers`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Teachers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `nick` varchar(45) COLLATE utf8_spanish_ci DEFAULT NULL,
  `classroom` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_Teachers_1_idx` (`classroom`),
  CONSTRAINT `fk_Teachers_1` FOREIGN KEY (`classroom`) REFERENCES `Classrooms` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tempEvents`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tempEvents` (
  `event_id` int(11) NOT NULL,
  `event_sub` int(11) NOT NULL,
  `event_type` int(11) DEFAULT NULL,
  `student` int(11) NOT NULL,
  `teacher` int(11) DEFAULT NULL,
  PRIMARY KEY (`event_id`,`event_sub`,`student`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tempIds`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tempIds` (
  `ids` int(11) NOT NULL,
  `teacher` int(11) DEFAULT NULL,
  PRIMARY KEY (`ids`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_spanish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'zgbpq88q_montessano'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-12-29  1:33:57
