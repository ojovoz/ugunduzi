-- phpMyAdmin SQL Dump
-- version 4.4.15.10
-- https://www.phpmyadmin.net
--
-- Servidor: 192.168.86.55
-- Tiempo de generación: 22-05-2018 a las 16:25:06
-- Versión del servidor: 5.5.57-0+deb7u1-log
-- Versión de PHP: 5.3.29-1~dotdeb.0

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `ugunduzi`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `crop`
--

CREATE TABLE IF NOT EXISTS `crop` (
  `crop_id` int(10) unsigned NOT NULL,
  `crop_name` varchar(30) NOT NULL,
  `crop_variety` varchar(30) NOT NULL,
  `crop_name_english` varchar(30) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `crop`
--

INSERT INTO `crop` (`crop_id`, `crop_name`, `crop_variety`, `crop_name_english`) VALUES
(1, 'Mahindi', '', 'Maize'),
(2, 'Mihogo', '', 'Cassava'),
(3, 'Choroko', '', 'Mung beans'),
(4, 'Alizeti', '', 'Sunflower'),
(5, 'Viazi vitamu', '', 'Sweet potato leaves');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `data_item`
--

CREATE TABLE IF NOT EXISTS `data_item` (
  `data_item_id` int(10) unsigned NOT NULL,
  `data_item_name` varchar(100) NOT NULL,
  `data_item_default_units_id` int(10) unsigned NOT NULL,
  `data_item_type` int(10) unsigned NOT NULL COMMENT '0=number, 1=date, 2=cost',
  `is_crop_specific` tinyint(1) NOT NULL,
  `is_treatment_specific` tinyint(1) NOT NULL,
  `data_item_name_english` varchar(30) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `data_item`
--

INSERT INTO `data_item` (`data_item_id`, `data_item_name`, `data_item_default_units_id`, `data_item_type`, `is_crop_specific`, `is_treatment_specific`, `data_item_name_english`) VALUES
(1, 'Maandalizi ya shamba', 0, 1, 0, 0, 'Land preparation'),
(2, 'Kupanda', 0, 1, 1, 0, 'Planting'),
(3, 'Kuvuna', 0, 1, 1, 0, 'Harvesting'),
(4, 'Mavuno', 1, 0, 1, 0, 'Yield'),
(5, 'Utumiaji (Matibabu)', 0, 1, 0, 1, 'Application (treatment)'),
(6, 'Bei (Mazao)', 4, 2, 1, 0, 'Costs (crop)'),
(7, 'Bei (Matibabu)', 4, 2, 0, 1, 'Costs (treatment)'),
(8, 'Mauzo', 4, 2, 1, 0, 'Sales');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `farm`
--

CREATE TABLE IF NOT EXISTS `farm` (
  `farm_id` int(10) unsigned NOT NULL,
  `user_id` int(10) unsigned NOT NULL,
  `farm_name` varchar(30) NOT NULL,
  `farm_size_acres` int(10) unsigned NOT NULL,
  `farm_date_created` date NOT NULL,
  `parent_farm_id` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `farm`
--

INSERT INTO `farm` (`farm_id`, `user_id`, `farm_name`, `farm_size_acres`, `farm_date_created`, `parent_farm_id`) VALUES
(52, 2, 'Shamba 3', 1, '2018-05-08', 0),
(55, 1, 'Shamba 1', 1, '2018-05-17', 51);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `log`
--

CREATE TABLE IF NOT EXISTS `log` (
  `log_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL,
  `log_date` date NOT NULL,
  `log_data_item_id` int(10) unsigned NOT NULL,
  `log_value` int(10) unsigned NOT NULL,
  `log_units_id` int(10) unsigned NOT NULL,
  `log_crop_id` int(10) unsigned NOT NULL,
  `log_treatment_id` int(10) unsigned NOT NULL,
  `log_picture` varchar(100) NOT NULL,
  `log_sound` varchar(100) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=163 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `log`
--

INSERT INTO `log` (`log_id`, `plot_id`, `log_date`, `log_data_item_id`, `log_value`, `log_units_id`, `log_crop_id`, `log_treatment_id`, `log_picture`, `log_sound`) VALUES
(65, 158, '2018-05-15', 0, 0, 0, 0, 0, '/img/img1.jpg', '/snd/snd1.mp3'),
(66, 162, '2018-05-14', 0, 0, 0, 0, 0, '/img/img66.jpg', '/snd/snd66.mp3'),
(121, 163, '2018-05-16', 0, 0, 0, 0, 0, '/img/img121.jpg', '/snd/snd121.mp3'),
(122, 163, '2018-05-16', 0, 0, 0, 0, 0, '/img/img122.jpg', '/snd/snd122.mp3'),
(142, 161, '2018-05-17', 0, 0, 0, 0, 0, '/img/img142.jpg', '/snd/snd142.mp3'),
(153, 162, '2018-05-16', 6, 5500, 4, 0, 0, '', ''),
(154, 160, '2018-05-15', 1, 0, 0, 0, 0, '', ''),
(155, 163, '2018-05-15', 6, 1000, 4, 0, 0, '', ''),
(156, 163, '2018-05-15', 7, 5000, 4, 0, 0, '', ''),
(157, 161, '2018-05-15', 1, 0, 0, 0, 0, '', ''),
(158, 158, '2018-05-15', 7, 5555, 4, 0, 2, '', ''),
(159, 159, '2018-05-09', 6, 1000, 4, 0, 0, '', ''),
(160, 159, '2018-05-09', 4, 25, 2, 0, 0, '', ''),
(161, 159, '2018-05-09', 7, 1000, 4, 0, 0, '', ''),
(162, 160, '2018-05-08', 6, 1000, 4, 0, 0, '', '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `plot`
--

CREATE TABLE IF NOT EXISTS `plot` (
  `plot_id` int(10) unsigned NOT NULL,
  `internal_plot_id` int(10) unsigned NOT NULL,
  `farm_id` int(10) unsigned NOT NULL,
  `plot_x` int(10) unsigned NOT NULL,
  `plot_y` int(10) unsigned NOT NULL,
  `plot_w` int(10) unsigned NOT NULL,
  `plot_h` int(10) unsigned NOT NULL,
  `plot_crop1` int(10) unsigned NOT NULL,
  `plot_crop2` int(10) unsigned NOT NULL,
  `plot_treatment1` int(10) unsigned NOT NULL,
  `plot_treatment2` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=172 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `plot`
--

INSERT INTO `plot` (`plot_id`, `internal_plot_id`, `farm_id`, `plot_x`, `plot_y`, `plot_w`, `plot_h`, `plot_crop1`, `plot_crop2`, `plot_treatment1`, `plot_treatment2`) VALUES
(158, 0, 52, 0, 0, 2, 4, 1, 0, 2, 1),
(159, 1, 52, 2, 0, 2, 1, 1, 0, 1, 0),
(160, 2, 52, 2, 1, 1, 2, 1, 0, 0, 0),
(161, 3, 52, 3, 1, 1, 1, 1, 0, 1, 0),
(162, 4, 52, 3, 2, 1, 1, 1, 0, 2, 0),
(163, 5, 52, 2, 3, 2, 1, 1, 0, 2, 0),
(170, 0, 55, 0, 0, 1, 1, 0, 0, 0, 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `treatment`
--

CREATE TABLE IF NOT EXISTS `treatment` (
  `treatment_id` int(10) unsigned NOT NULL,
  `treatment_name` varchar(30) NOT NULL,
  `treatment_category` tinyint(3) unsigned NOT NULL,
  `treatment_name_english` varchar(30) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `treatment`
--

INSERT INTO `treatment` (`treatment_id`, `treatment_name`, `treatment_category`, `treatment_name_english`) VALUES
(1, 'Uthibiti wa wadudu', 0, 'Pest control'),
(2, 'Usimamizi wa udongo', 1, 'Soil management');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `units`
--

CREATE TABLE IF NOT EXISTS `units` (
  `units_id` int(10) unsigned NOT NULL,
  `units_name` varchar(100) NOT NULL,
  `units_type` int(10) unsigned NOT NULL COMMENT '0=number, 1=date, 2=cost',
  `units_name_english` varchar(30) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `units`
--

INSERT INTO `units` (`units_id`, `units_name`, `units_type`, `units_name_english`) VALUES
(1, 'Kg', 0, 'Kg'),
(2, 'Debe', 0, 'Baskets'),
(4, 'TZS', 2, 'TZS');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `user_id` int(10) unsigned NOT NULL,
  `user_name` varchar(100) NOT NULL,
  `user_alias` varchar(30) NOT NULL,
  `user_password` varchar(30) NOT NULL,
  `user_mobile` varchar(30) NOT NULL,
  `user_group` varchar(30) NOT NULL,
  `user_association` varchar(30) NOT NULL,
  `user_location` varchar(30) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `user`
--

INSERT INTO `user` (`user_id`, `user_name`, `user_alias`, `user_password`, `user_mobile`, `user_group`, `user_association`, `user_location`) VALUES
(1, 'Test User', 'test', 'test', '', '', '', ''),
(2, 'Eugenio Tisselli', 'eugenio', 'cubo23', '', '', '', ''),
(5, 'Elias Charles Maajire', 'elias', 'maajire', '', '', '', 'Morogoro'),
(6, 'Betina J. Mgwama', 'betina', 'mgwama', '', '', '', 'Morogoro'),
(7, 'Teddy Kaweah', 'teddy', 'kaweah', '', '', '', 'Morogoro'),
(8, 'Faudhia Magawa', 'faudhia', 'magawa', '', '', '', 'Morogoro'),
(9, 'Tausi Omary Juma', 'tausi', 'juma', '', '', '', 'Morogoro'),
(10, 'Eugenia K. Kishogo', 'eugenia', 'kishogo', '', '', '', 'Morogoro'),
(11, 'Tabu Said', 'tabu', 'said', '', '', '', 'Morogoro'),
(12, 'Sylvester Letus', 'sylvester', 'letus', '', '', '', 'Morogoro'),
(13, 'Christopher Benagire', 'christopher', 'benagire', '', '', '', 'Morogoro'),
(14, 'Parkursi Ngobwa', 'parkursi', 'ngobwa', '', '', '', 'Morogoro'),
(15, 'Hamisi R. Shomari', 'hamisi r', 'shomari', '', '', '', 'Morogoro'),
(16, 'Abdallah Jumanne', 'abdallah j', 'jumanne', '', '', '', 'Chambezi'),
(17, 'Ana Macha', 'ana', 'macha', '', '', '', 'Chambezi'),
(18, 'Renalda Msaki', 'renalda', 'msaki', '', '', '', 'Chambezi'),
(19, 'Rehema Maganga', 'rehema', 'maganga', '', '', '', 'Chambezi'),
(20, 'Nuru Mohamedi', 'nuru', 'mohamedi', '', '', '', 'Chambezi'),
(21, 'Fatuma Ngomero', 'fatuma', 'ngomero', '', '', '', 'Chambezi'),
(22, 'Mwanaidi Shabani', 'mwanaidi', 'shabani', '', '', '', 'Chambezi'),
(23, 'Hamisi Palango', 'hamisi', 'palango', '', '', '', 'Chambezi'),
(24, 'Fadhili Salum', 'fadhili', 'salum', '', '', '', 'Chambezi'),
(25, 'Abdallah Mahmudu', 'abdallah m', 'mahmudu', '', '', '', 'Chambezi'),
(26, 'Tumaini Mussa', 'tumaini', 'mussa', '', '', '', 'Masasi'),
(27, 'Hadija Wende', 'hadija', 'wende', '', '', '', 'Masasi'),
(28, 'Mohamedi Simosya', 'mohamedi', 'simosya', '', '', '', 'Masasi'),
(29, 'Omari Aleka', 'omari', 'aleka', '', '', '', 'Masasi'),
(30, 'Regina Hamisi', 'regina', 'hamisi', '', '', '', 'Masasi'),
(31, 'Zena Rajabu', 'zena', 'rajabu', '', '', '', 'Masasi'),
(32, 'Salima Mponda', 'salima', 'mponda', '', '', '', 'Masasi'),
(33, 'Laina Selemani', 'laina', 'selemani', '', '', '', 'Masasi');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `crop`
--
ALTER TABLE `crop`
  ADD PRIMARY KEY (`crop_id`);

--
-- Indices de la tabla `data_item`
--
ALTER TABLE `data_item`
  ADD PRIMARY KEY (`data_item_id`);

--
-- Indices de la tabla `farm`
--
ALTER TABLE `farm`
  ADD PRIMARY KEY (`farm_id`);

--
-- Indices de la tabla `log`
--
ALTER TABLE `log`
  ADD PRIMARY KEY (`log_id`);

--
-- Indices de la tabla `plot`
--
ALTER TABLE `plot`
  ADD PRIMARY KEY (`plot_id`);

--
-- Indices de la tabla `treatment`
--
ALTER TABLE `treatment`
  ADD PRIMARY KEY (`treatment_id`);

--
-- Indices de la tabla `units`
--
ALTER TABLE `units`
  ADD PRIMARY KEY (`units_id`);

--
-- Indices de la tabla `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `crop`
--
ALTER TABLE `crop`
  MODIFY `crop_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=6;
--
-- AUTO_INCREMENT de la tabla `data_item`
--
ALTER TABLE `data_item`
  MODIFY `data_item_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=9;
--
-- AUTO_INCREMENT de la tabla `farm`
--
ALTER TABLE `farm`
  MODIFY `farm_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=57;
--
-- AUTO_INCREMENT de la tabla `log`
--
ALTER TABLE `log`
  MODIFY `log_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=163;
--
-- AUTO_INCREMENT de la tabla `plot`
--
ALTER TABLE `plot`
  MODIFY `plot_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=172;
--
-- AUTO_INCREMENT de la tabla `treatment`
--
ALTER TABLE `treatment`
  MODIFY `treatment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT de la tabla `units`
--
ALTER TABLE `units`
  MODIFY `units_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT de la tabla `user`
--
ALTER TABLE `user`
  MODIFY `user_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=34;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
