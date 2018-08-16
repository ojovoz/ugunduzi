-- phpMyAdmin SQL Dump
-- version 4.4.15.10
-- https://www.phpmyadmin.net
--
-- Servidor: 192.168.86.197
-- Tiempo de generación: 16-08-2018 a las 19:18:49
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
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `crop`
--

INSERT INTO `crop` (`crop_id`, `crop_name`, `crop_variety`, `crop_name_english`) VALUES
(1, 'Mahindi', '', 'Maize'),
(2, 'Mihogo', '', 'Cassava'),
(3, 'Choroko', '', 'Mung beans'),
(4, 'Alizeti', '', 'Sunflower'),
(5, 'Viazi vitamu', '', 'Sweet potato leaves'),
(6, 'Ufuta', '', 'Sesame'),
(7, 'Kunde', '', 'Pigeon peas'),
(8, 'Mpunga', '', 'Rice'),
(9, 'Matango', '', 'Cucumber'),
(10, 'Bamia', '', 'Okra'),
(11, 'Mipapai', '', 'Papaya'),
(12, 'Mchungwa', '', 'Orange'),
(13, 'Miembe', '', 'Mango'),
(14, 'Matembele', '', 'Sweet Potato Leaves'),
(15, 'Migomba', '', 'Banana'),
(16, 'Mbaazi', '', 'Peas'),
(17, 'Karanga', '', 'Nuts'),
(18, 'Nyanya', '', 'Tomato'),
(19, 'Mkorosho', '', 'Cashew nuts'),
(20, 'Mchicha', '', 'Amaranth'),
(21, 'Matikiti', '', 'Melon'),
(23, 'Nyanya chungu', '', 'African eggplant'),
(24, 'Biliganya', '', 'Eggplant'),
(26, 'Maboga', '', 'Pumpkin'),
(27, 'Maharage', '', 'Beans'),
(28, 'Vitunguu maji', '', 'Tender garlic'),
(29, 'Vitunguu swamu', '', 'Garlic'),
(30, 'Mtama', '', 'Millet'),
(31, 'Pilipili hoho', '', 'Bell pepper'),
(32, 'Rozela', '', 'Rosella'),
(33, 'Kabichi', '', 'Cabbage'),
(34, 'Aloe vera', '', 'Aloe vera'),
(35, 'Mwarubaini', '', 'Neem'),
(36, 'Korosho', '', 'Cashew'),
(37, 'Milonge', '', ''),
(38, 'Utupa', '', ''),
(39, 'Mkuna', '', ''),
(40, 'Fiwi', '', 'Lima beans'),
(41, 'Njugu', '', 'Bambara nuts'),
(42, 'Upupu', '', ''),
(43, 'Pasheni', '', 'Passion fruit'),
(44, 'Ulezi', '', ''),
(45, 'Mnafu', '', ''),
(46, 'Figiri', '', 'Fig'),
(47, 'Karoti', '', 'Carrots');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `crop_x_plot`
--

CREATE TABLE IF NOT EXISTS `crop_x_plot` (
  `crop_x_plot_id` int(10) unsigned NOT NULL,
  `crop_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `crop_x_plot`
--

INSERT INTO `crop_x_plot` (`crop_x_plot_id`, `crop_id`, `plot_id`) VALUES
(1, 1, 1),
(2, 3, 1),
(3, 4, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `data_item`
--

CREATE TABLE IF NOT EXISTS `data_item` (
  `data_item_id` int(10) unsigned NOT NULL,
  `data_item_name` varchar(100) NOT NULL,
  `data_item_default_units_id` int(10) unsigned NOT NULL,
  `data_item_type` int(10) unsigned NOT NULL COMMENT '0=activity (cost), 1=activity (cost with quantity), 2=input (cost), 3=output (sale)',
  `is_crop_specific` tinyint(1) NOT NULL,
  `is_treatment_specific` tinyint(1) NOT NULL,
  `data_item_name_english` varchar(30) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `data_item`
--

INSERT INTO `data_item` (`data_item_id`, `data_item_name`, `data_item_default_units_id`, `data_item_type`, `is_crop_specific`, `is_treatment_specific`, `data_item_name_english`) VALUES
(1, 'Maandalizi ya shamba', 0, 0, 0, 0, 'Land preparation'),
(2, 'Kupanda', 0, 0, 1, 0, 'Planting'),
(3, 'Kuvuna', 0, 0, 1, 0, 'Harvesting'),
(4, 'Mavuno', 1, 3, 1, 0, 'Yield'),
(5, 'Utumiaji (Matibabu)', 0, 1, 0, 1, 'Application (treatment)'),
(6, 'Mbegu', 4, 2, 1, 0, 'Seed cost'),
(7, 'Bei (Matibabu)', 4, 2, 0, 1, 'Costs (treatment)'),
(8, 'Mauzo', 4, 3, 1, 0, 'Sales'),
(9, 'Palizi', 0, 0, 0, 0, 'Weeding'),
(10, 'Kuandoa', 0, 0, 0, 0, 'Pruning'),
(11, 'Kuhifadi', 0, 0, 1, 0, 'Storage'),
(12, 'Usafiri', 0, 0, 1, 0, 'Transport'),
(13, 'Kodi', 0, 0, 0, 0, 'Rent'),
(14, 'Chombo', 0, 2, 0, 0, 'Tool');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `farm`
--

CREATE TABLE IF NOT EXISTS `farm` (
  `farm_id` int(10) unsigned NOT NULL,
  `farm_app_id` int(10) unsigned NOT NULL,
  `user_id` int(10) unsigned NOT NULL,
  `farm_name` varchar(30) NOT NULL,
  `farm_size_acres` float unsigned NOT NULL,
  `farm_date_created` date NOT NULL,
  `farm_version` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `farm`
--

INSERT INTO `farm` (`farm_id`, `farm_app_id`, `user_id`, `farm_name`, `farm_size_acres`, `farm_date_created`, `farm_version`) VALUES
(1, 0, 2, 'Shamba 1', 1, '2018-08-16', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `log`
--

CREATE TABLE IF NOT EXISTS `log` (
  `log_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL,
  `log_date` date NOT NULL,
  `log_data_item_id` int(10) unsigned NOT NULL,
  `log_quantity` decimal(10,0) unsigned NOT NULL,
  `log_value` float unsigned NOT NULL,
  `log_units_id` int(10) unsigned NOT NULL,
  `log_crop_id` int(10) unsigned NOT NULL,
  `log_treatment_id` int(10) unsigned NOT NULL,
  `log_comments` text NOT NULL,
  `log_picture` varchar(100) NOT NULL,
  `log_sound` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  `plot_h` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `plot`
--

INSERT INTO `plot` (`plot_id`, `internal_plot_id`, `farm_id`, `plot_x`, `plot_y`, `plot_w`, `plot_h`) VALUES
(1, 0, 1, 0, 0, 4, 2),
(2, 1, 1, 0, 2, 4, 2);

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
(1, 'Dawa', 0, 'Pest control'),
(2, 'Mbolea', 1, 'Soil management');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `treatment_ingredient`
--

CREATE TABLE IF NOT EXISTS `treatment_ingredient` (
  `treatment_ingredient_id` int(10) unsigned NOT NULL,
  `treatment_id` int(10) unsigned NOT NULL,
  `treatment_ingredient_name` varchar(100) NOT NULL,
  `treatment_ingredient_name_english` varchar(100) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `treatment_ingredient`
--

INSERT INTO `treatment_ingredient` (`treatment_ingredient_id`, `treatment_id`, `treatment_ingredient_name`, `treatment_ingredient_name_english`) VALUES
(1, 1, 'Pilipili', ''),
(2, 1, 'Aloe vera', ''),
(3, 1, 'Mwarubaini', ''),
(4, 1, 'Sabuni', ''),
(5, 1, 'Vitunguu', ''),
(6, 1, 'Tangawizi', ''),
(7, 1, 'Majivu', ''),
(8, 1, 'Mafuta ya taa', ''),
(9, 1, 'Maziwa', ''),
(10, 2, 'Matandazo', ''),
(11, 2, 'Samadi', ''),
(12, 2, 'Mboji', ''),
(13, 1, 'Mpapai', ''),
(14, 1, 'Utupa', ''),
(15, 1, 'Mbangi', ''),
(16, 1, 'Mvepe', ''),
(17, 1, 'Lantana camara', ''),
(18, 2, 'Chai mbolea', ''),
(19, 2, 'Majivu', '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `treatment_ingredient_x_plot`
--

CREATE TABLE IF NOT EXISTS `treatment_ingredient_x_plot` (
  `treatment_ingredient_x_plot` int(10) unsigned NOT NULL,
  `treatment_ingredient_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `treatment_ingredient_x_plot`
--

INSERT INTO `treatment_ingredient_x_plot` (`treatment_ingredient_x_plot`, `treatment_ingredient_id`, `plot_id`) VALUES
(1, 3, 1),
(2, 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `units`
--

CREATE TABLE IF NOT EXISTS `units` (
  `units_id` int(10) unsigned NOT NULL,
  `units_name` varchar(100) NOT NULL,
  `units_type` int(10) unsigned NOT NULL COMMENT '0=number, 1=date, 2=cost',
  `units_name_english` varchar(30) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `units`
--

INSERT INTO `units` (`units_id`, `units_name`, `units_type`, `units_name_english`) VALUES
(1, 'Kg', 0, 'Kg'),
(2, 'Debe', 0, 'Baskets'),
(4, 'TZS', 2, 'TZS'),
(5, 'Gunia', 0, '100 kg package'),
(6, 'Tenga', 0, 'Variable weight');

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
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8;

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
(33, 'Laina Selemani', 'laina', 'selemani', '', '', '', 'Masasi'),
(34, 'Mable Mandova', 'mable', 'david', '', '', '', 'Masasi'),
(35, 'Sebastian Ndimbo', 'sebastian', 'ndimbo', '', '', '', 'Masasi');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `crop`
--
ALTER TABLE `crop`
  ADD PRIMARY KEY (`crop_id`);

--
-- Indices de la tabla `crop_x_plot`
--
ALTER TABLE `crop_x_plot`
  ADD PRIMARY KEY (`crop_x_plot_id`);

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
-- Indices de la tabla `treatment_ingredient`
--
ALTER TABLE `treatment_ingredient`
  ADD PRIMARY KEY (`treatment_ingredient_id`);

--
-- Indices de la tabla `treatment_ingredient_x_plot`
--
ALTER TABLE `treatment_ingredient_x_plot`
  ADD PRIMARY KEY (`treatment_ingredient_x_plot`);

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
  MODIFY `crop_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=48;
--
-- AUTO_INCREMENT de la tabla `crop_x_plot`
--
ALTER TABLE `crop_x_plot`
  MODIFY `crop_x_plot_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT de la tabla `data_item`
--
ALTER TABLE `data_item`
  MODIFY `data_item_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=15;
--
-- AUTO_INCREMENT de la tabla `farm`
--
ALTER TABLE `farm`
  MODIFY `farm_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- AUTO_INCREMENT de la tabla `log`
--
ALTER TABLE `log`
  MODIFY `log_id` int(10) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT de la tabla `plot`
--
ALTER TABLE `plot`
  MODIFY `plot_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT de la tabla `treatment`
--
ALTER TABLE `treatment`
  MODIFY `treatment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT de la tabla `treatment_ingredient`
--
ALTER TABLE `treatment_ingredient`
  MODIFY `treatment_ingredient_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=20;
--
-- AUTO_INCREMENT de la tabla `treatment_ingredient_x_plot`
--
ALTER TABLE `treatment_ingredient_x_plot`
  MODIFY `treatment_ingredient_x_plot` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT de la tabla `units`
--
ALTER TABLE `units`
  MODIFY `units_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=7;
--
-- AUTO_INCREMENT de la tabla `user`
--
ALTER TABLE `user`
  MODIFY `user_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=36;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
