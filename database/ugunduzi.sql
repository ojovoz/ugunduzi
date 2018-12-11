-- phpMyAdmin SQL Dump
-- version 4.4.15.10
-- https://www.phpmyadmin.net
--
-- Servidor: 192.168.86.197
-- Tiempo de generación: 11-12-2018 a las 20:00:56
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
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8;

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
(12, 'Michungwa', '', 'Orange'),
(13, 'Miembe', '', 'Mango'),
(14, 'Matembele', '', 'Sweet Potato Leaves'),
(15, 'Migomba', '', 'Banana'),
(16, 'Mbaazi', '', 'Peas'),
(17, 'Karanga', '', 'Nuts'),
(18, 'Nyanya', '', 'Tomato'),
(19, 'Mikorosho', '', 'Cashew nuts'),
(20, 'Mchicha', '', 'Amaranth'),
(21, 'Matikiti', '', 'Melon'),
(23, 'Nyanya chungu', '', 'African eggplant'),
(24, 'Bilinganya', '', 'Eggplant'),
(26, 'Maboga', '', 'Pumpkin'),
(27, 'Maharage', '', 'Beans'),
(28, 'Vitunguu maji', '', 'Tender garlic'),
(29, 'Vitunguu swaumu', '', 'Garlic'),
(30, 'Mtama', '', 'Millet'),
(31, 'Pilipili hoho', '', 'Bell pepper'),
(32, 'Rozela', '', 'Rosella'),
(33, 'Kabichi', '', 'Cabbage'),
(34, 'Aloe vera', '', 'Aloe vera'),
(35, 'Mwarubaini', '', 'Neem'),
(36, 'Korosho', '', 'Cashew'),
(37, 'Milonge', '', 'Milonge'),
(38, 'Utupa', '', 'Utupa'),
(39, 'Mkuna', '', 'Mkuna beans'),
(40, 'Fiwi', '', 'Lima beans'),
(41, 'Njugu', '', 'Bambara nuts'),
(42, 'Upupu', '', 'Upupu'),
(43, 'Pasheni', '', 'Passion fruit'),
(44, 'Ulezi', '', 'Ulezi'),
(45, 'Mnavu', '', 'Mnavu'),
(46, 'Figiri', '', 'Fig'),
(47, 'Karoti', '', 'Carrots'),
(48, 'Pilipili', '', 'Chili'),
(49, 'Nanasi', '', 'Pineapple');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `crop_x_plot`
--

CREATE TABLE IF NOT EXISTS `crop_x_plot` (
  `crop_x_plot_id` int(10) unsigned NOT NULL,
  `crop_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=660 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `crop_x_plot`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `data_item`
--

CREATE TABLE IF NOT EXISTS `data_item` (
  `data_item_id` int(10) unsigned NOT NULL,
  `data_item_name` varchar(100) NOT NULL,
  `data_item_default_units_id` int(10) unsigned NOT NULL,
  `data_item_type` int(10) unsigned NOT NULL COMMENT '0=cost, 1=quantity & units, 2=cost, quantity, units',
  `is_crop_specific` tinyint(1) NOT NULL,
  `is_treatment_specific` tinyint(1) NOT NULL,
  `data_item_name_english` varchar(30) NOT NULL,
  `is_retroactive` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `data_item`
--

INSERT INTO `data_item` (`data_item_id`, `data_item_name`, `data_item_default_units_id`, `data_item_type`, `is_crop_specific`, `is_treatment_specific`, `data_item_name_english`, `is_retroactive`) VALUES
(1, 'Maandalizi ya shamba', 0, 0, 0, 0, 'Land preparation', 0),
(2, 'Kupanda', 0, 0, 1, 0, 'Planting', 0),
(4, 'Mavuno', 1, 1, 1, 0, 'Yield', 0),
(5, 'Utumiaji (Matibabu)', 1, 1, 0, 1, 'Application (treatment)', 0),
(6, 'Bei (Mbegu)', 1, 2, 1, 0, 'Costs (seeds)', 0),
(7, 'Bei (Matibabu)', 1, 2, 0, 1, 'Costs (treatment)', 0),
(8, 'Mauzo', 1, 3, 1, 0, 'Sales', 1),
(9, 'Palizi', 0, 0, 0, 0, 'Weeding', 0),
(10, 'Kupogolea', 0, 0, 1, 0, 'Pruning', 0),
(11, 'Kuhifadhi', 0, 0, 1, 0, 'Storage', 1),
(12, 'Usafiri', 0, 0, 1, 0, 'Transport', 0),
(13, 'Kodi', 0, 0, 0, 0, 'Rent', 0),
(14, 'Chombo', 0, 0, 0, 0, 'Tool', 0),
(15, 'Gharama nyingine', 1, 0, 0, 0, 'Other costs', 1),
(16, 'Faida nyingine', 1, 4, 0, 0, 'Other benefits', 1);

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
) ENGINE=InnoDB AUTO_INCREMENT=182 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `farm`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `log`
--

CREATE TABLE IF NOT EXISTS `log` (
  `log_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL,
  `log_date` date NOT NULL,
  `log_data_item_id` int(10) unsigned NOT NULL,
  `log_quantity` float unsigned NOT NULL,
  `log_value` float unsigned NOT NULL,
  `log_units_id` int(10) unsigned NOT NULL,
  `log_crop_id` int(10) unsigned NOT NULL,
  `log_treatment_id` int(10) unsigned NOT NULL,
  `log_comments` text NOT NULL,
  `log_picture` varchar(100) NOT NULL,
  `log_sound` varchar(100) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=557 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `log`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=430 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `plot`
--

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
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `treatment_ingredient`
--

INSERT INTO `treatment_ingredient` (`treatment_ingredient_id`, `treatment_id`, `treatment_ingredient_name`, `treatment_ingredient_name_english`) VALUES
(1, 1, 'Pilipili', 'Pepper'),
(2, 1, 'Aloe vera', 'Aloe vera'),
(3, 1, 'Mwarubaini', 'Neem'),
(4, 1, 'Sabuni', 'Soap'),
(5, 1, 'Vitunguu swaumu', 'Garlic'),
(6, 1, 'Tangawizi', 'Ginger'),
(7, 1, 'Majivu', 'Ashes'),
(8, 1, 'Mafuta ya taa', 'Kerosene'),
(9, 1, 'Maziwa', 'Milk'),
(10, 2, 'Matandazo', 'Grasses'),
(11, 2, 'Samadi', 'Manure'),
(12, 2, 'Mboji', 'Compost'),
(13, 1, 'Mipapai', 'Papaya'),
(14, 1, 'Utupa', 'Utupa'),
(15, 1, 'Bangi', 'Hemp'),
(16, 1, 'Mvepe', 'Mvepe'),
(17, 1, 'Lantana camara', 'Lanatana camara'),
(18, 2, 'Chai mbolea', 'Tea manure'),
(19, 2, 'Majivu', 'Ashes'),
(20, 1, 'Vitunguu maji', 'Onion'),
(21, 1, 'Mnyaa', 'Mnyaa');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `treatment_ingredient_x_plot`
--

CREATE TABLE IF NOT EXISTS `treatment_ingredient_x_plot` (
  `treatment_ingredient_x_plot` int(10) unsigned NOT NULL,
  `treatment_ingredient_id` int(10) unsigned NOT NULL,
  `plot_id` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=878 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `treatment_ingredient_x_plot`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `units`
--

CREATE TABLE IF NOT EXISTS `units` (
  `units_id` int(10) unsigned NOT NULL,
  `units_name` varchar(100) NOT NULL,
  `units_type` int(10) unsigned NOT NULL COMMENT '0=number, 1=date, 2=cost',
  `units_name_english` varchar(30) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `units`
--

INSERT INTO `units` (`units_id`, `units_name`, `units_type`, `units_name_english`) VALUES
(1, 'Kg', 0, 'Kg'),
(2, 'Debe', 0, 'Baskets'),
(4, 'TZS', 2, 'TZS'),
(5, 'Gunia', 0, '100 kg package'),
(6, 'Tenga', 0, 'Variable weight'),
(7, 'Lita', 0, 'Liters'),
(8, 'Mti', 0, 'Sticks');

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
  `user_location` varchar(30) NOT NULL,
  `is_admin` tinyint(3) unsigned NOT NULL DEFAULT '0'
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `user`
--

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
  MODIFY `crop_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=50;
--
-- AUTO_INCREMENT de la tabla `crop_x_plot`
--
ALTER TABLE `crop_x_plot`
  MODIFY `crop_x_plot_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=660;
--
-- AUTO_INCREMENT de la tabla `data_item`
--
ALTER TABLE `data_item`
  MODIFY `data_item_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=17;
--
-- AUTO_INCREMENT de la tabla `farm`
--
ALTER TABLE `farm`
  MODIFY `farm_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=182;
--
-- AUTO_INCREMENT de la tabla `log`
--
ALTER TABLE `log`
  MODIFY `log_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=557;
--
-- AUTO_INCREMENT de la tabla `plot`
--
ALTER TABLE `plot`
  MODIFY `plot_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=430;
--
-- AUTO_INCREMENT de la tabla `treatment`
--
ALTER TABLE `treatment`
  MODIFY `treatment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT de la tabla `treatment_ingredient`
--
ALTER TABLE `treatment_ingredient`
  MODIFY `treatment_ingredient_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=22;
--
-- AUTO_INCREMENT de la tabla `treatment_ingredient_x_plot`
--
ALTER TABLE `treatment_ingredient_x_plot`
  MODIFY `treatment_ingredient_x_plot` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=878;
--
-- AUTO_INCREMENT de la tabla `units`
--
ALTER TABLE `units`
  MODIFY `units_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=9;
--
-- AUTO_INCREMENT de la tabla `user`
--
ALTER TABLE `user`
  MODIFY `user_id` int(10) unsigned NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=49;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
