-- ----------------------------------------------------------------------------
-- MySQL Workbench Migration
-- Migrated Schemata: FoldingCoin
-- Source Schemata: FoldingCoin
-- Created: Wed Jun  6 00:42:33 2018
-- Workbench Version: 6.3.10
-- ----------------------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- Schema FoldingCoin
-- ----------------------------------------------------------------------------
DROP SCHEMA IF EXISTS `FoldingCoin` ;
CREATE SCHEMA IF NOT EXISTS `FoldingCoin` ;

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.Downloads
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`Downloads` (
  `DownloadId` INT NOT NULL AUTO_INCREMENT,
  `StatusId` INT NOT NULL,
  `FileId` INT NOT NULL,
  `DownloadDateTime` DATETIME(6) NOT NULL,
  PRIMARY KEY (`DownloadId`),
  CONSTRAINT `FK_Downloads_Files`
    FOREIGN KEY (`FileId`)
    REFERENCES `FoldingCoin`.`Files` (`FileId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_Downloads_Statuses`
    FOREIGN KEY (`StatusId`)
    REFERENCES `FoldingCoin`.`Statuses` (`StatusId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.FAHData
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`FAHData` (
  `FAHDataId` INT NOT NULL AUTO_INCREMENT,
  `UserName` VARCHAR(50) CHARACTER SET 'utf8mb4' NOT NULL,
  `TotalPoints` BIGINT NOT NULL,
  `WorkUnits` BIGINT NOT NULL,
  `TeamNumber` BIGINT NOT NULL,
  PRIMARY KEY (`FAHDataId`),
  CONSTRAINT `FK_FAHData_Users`
    FOREIGN KEY (`UserName`)
    REFERENCES `FoldingCoin`.`Users` (`UserName`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.FAHDataRuns
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`FAHDataRuns` (
  `FAHDataRunId` INT NOT NULL AUTO_INCREMENT,
  `FAHDataId` INT NOT NULL,
  `DownloadId` INT NOT NULL,
  `TeamMemberId` INT NOT NULL,
  PRIMARY KEY (`FAHDataRunId`),
  CONSTRAINT `FK_FAHDataRuns_Downloads`
    FOREIGN KEY (`DownloadId`)
    REFERENCES `FoldingCoin`.`Downloads` (`DownloadId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_FAHDataRuns_FAHData`
    FOREIGN KEY (`FAHDataId`)
    REFERENCES `FoldingCoin`.`FAHData` (`FAHDataId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_FAHDataRuns_TeamMembers`
    FOREIGN KEY (`TeamMemberId`)
    REFERENCES `FoldingCoin`.`TeamMembers` (`TeamMemberId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.Files
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`Files` (
  `FileId` INT NOT NULL AUTO_INCREMENT,
  `FileName` VARCHAR(50) CHARACTER SET 'utf8mb4' NULL,
  `FileExtension` VARCHAR(5) CHARACTER SET 'utf8mb4' NULL,
  `FileData` LONGTEXT CHARACTER SET 'utf8mb4' NULL,
  PRIMARY KEY (`FileId`));

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.Rejections
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`Rejections` (
  `RejectionId` INT NOT NULL AUTO_INCREMENT,
  `DownloadId` INT NOT NULL,
  `LineNumber` INT NULL,
  `Reason` VARCHAR(500) CHARACTER SET 'utf8mb4' NOT NULL,
  PRIMARY KEY (`RejectionId`),
  CONSTRAINT `FK_Rejections_Downloads`
    FOREIGN KEY (`DownloadId`)
    REFERENCES `FoldingCoin`.`Downloads` (`DownloadId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.Statuses
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`Statuses` (
  `StatusId` INT NOT NULL AUTO_INCREMENT,
  `Status` VARCHAR(50) CHARACTER SET 'utf8mb4' NOT NULL,
  `StatusDescription` VARCHAR(100) CHARACTER SET 'utf8mb4' NOT NULL,
  PRIMARY KEY (`StatusId`));

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.TeamMembers
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`TeamMembers` (
  `TeamMemberId` INT NOT NULL AUTO_INCREMENT,
  `TeamId` INT NOT NULL,
  `UserId` INT NOT NULL,
  PRIMARY KEY (`TeamMemberId`),
  CONSTRAINT `FK_TeamMembers_Teams`
    FOREIGN KEY (`TeamId`)
    REFERENCES `FoldingCoin`.`Teams` (`TeamId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_TeamMembers_Users`
    FOREIGN KEY (`UserId`)
    REFERENCES `FoldingCoin`.`Users` (`UserId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.Teams
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`Teams` (
  `TeamId` INT NOT NULL AUTO_INCREMENT,
  `TeamNumber` BIGINT NOT NULL,
  `TeamName` VARCHAR(150) CHARACTER SET 'utf8mb4' NULL,
  PRIMARY KEY (`TeamId`));

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.Users
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`Users` (
  `UserId` INT NOT NULL AUTO_INCREMENT,
  `UserName` VARCHAR(50) CHARACTER SET 'utf8mb4' NOT NULL,
  `FriendlyName` VARCHAR(50) CHARACTER SET 'utf8mb4' NULL,
  `BitcoinAddress` VARCHAR(50) CHARACTER SET 'utf8mb4' NULL,
  PRIMARY KEY (`UserId`),
  UNIQUE INDEX `IX_Users` (`UserName` ASC));

-- ----------------------------------------------------------------------------
-- Table FoldingCoin.UserStats
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FoldingCoin`.`UserStats` (
  `UserStatId` INT NOT NULL AUTO_INCREMENT,
  `FAHDataRunId` INT NOT NULL,
  `Points` BIGINT NOT NULL,
  `WorkUnits` BIGINT NOT NULL,
  PRIMARY KEY (`UserStatId`),
  CONSTRAINT `FK_UserStats_FAHDataRun`
    FOREIGN KEY (`FAHDataRunId`)
    REFERENCES `FoldingCoin`.`FAHDataRuns` (`FAHDataRunId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
SET FOREIGN_KEY_CHECKS = 1;
