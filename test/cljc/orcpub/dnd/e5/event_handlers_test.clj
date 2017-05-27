(ns orcpub.dnd.e5.event-handlers-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.data :refer [diff]]
            [orcpub.entity :as entity]
            [orcpub.template :as t]
            [orcpub.entity.strict :as se]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.event-handlers :as eh]))

(def character
  {:orcpub.entity/options
   {:class
    [{:orcpub.entity/key :barbarian,
      :db/id 17592186055267,
      :orcpub.entity/options
      {:levels
       [{:orcpub.entity/key :level-1, :db/id 17592186055269}
        {:orcpub.entity/key :level-2,
         :db/id 17592186055270,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :roll,
           :db/id 17592186055272,
           :orcpub.entity/value 7}}}
        {:orcpub.entity/key :level-3,
         :db/id 17592186055273,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :roll,
           :db/id 17592186055275,
           :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-4,
         :db/id 17592186055288
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :manual-entry
           :db/id 17592186055290,
           :orcpub.entity/value 1}}}]}}]}
   :db/id 17592186055262})

(defn level-key [level]
  (keyword (str "level-" level)))

(defn test-set-level [c new-level]
  (let [updated (eh/set-class-level c [:set-class-level 0 new-level])
        levels (-> updated
                   ::entity/options
                   :class
                   (get 0)
                   ::entity/options
                   :levels)]
    (is (= (level-key new-level)
           (-> levels
               last
               ::entity/key)))
    (is (= (map level-key (range 1 (inc new-level)))
           (map ::entity/key levels)))
    updated))

(deftest set-class-level--add-level
  (test-set-level character 5))

(deftest set-class-level--add-multiple-levels
  (test-set-level character 20))

(deftest set-class-level--remove-level
  (test-set-level character 3))

(deftest set-class-level--level-1
  (test-set-level character 1))

(deftest set-class-level--same-level
  (test-set-level character 4))

(def levels-strict-path [::se/selections 0 ::se/options 0 ::se/selections 0])

(defn get-id [e path]
  (-> e
      (get-in path)
      meta
      :db/id))

(defn get-levels-id [e]
  (get-id e [::entity/options :class 0 ::entity/options :levels]))

(defn get-classes-id [e]
  (get-id e [::entity/options :class]))

(defn get-skills-id [e]
  (get-id e [::entity/options :skill-profs]))

(defn get-race-id [e]
  (get-id e [::entity/options :race]))

(deftest test-set-level--round-trip
  (let [strict {:db/id 17592186055262, :orcpub.entity.strict/selections [{:db/id 17592186055266, :orcpub.entity.strict/key :class, :orcpub.entity.strict/options [{:db/id 17592186055267, :orcpub.entity.strict/key :barbarian, :orcpub.entity.strict/selections [{:db/id 17592186055268, :orcpub.entity.strict/key :levels, :orcpub.entity.strict/options [{:db/id 17592186055269, :orcpub.entity.strict/key :level-1} {:db/id 17592186055270, :orcpub.entity.strict/key :level-2, :orcpub.entity.strict/selections [{:db/id 17592186055271, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055272, :orcpub.entity.strict/key :roll, :orcpub.entity.strict/int-value 7}}]} {:db/id 17592186055273, :orcpub.entity.strict/key :level-3, :orcpub.entity.strict/selections [{:db/id 17592186055274, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055275, :orcpub.entity.strict/key :roll, :orcpub.entity.strict/int-value 5}}]} {:db/id 17592186055288, :orcpub.entity.strict/key :level-4, :orcpub.entity.strict/selections [{:db/id 17592186055289, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055290, :orcpub.entity.strict/key :manual-entry, :orcpub.entity.strict/int-value 1}}]}]}]}]}]}
        non-strict (entity/from-strict strict)
        updated (test-set-level non-strict 7)
        back-to-strict (entity/to-strict updated)
        without-new-levels (update-in back-to-strict
                                      (conj levels-strict-path ::se/options)
                                      (fn [levels]
                                        (with-meta
                                          (vec (take 4 levels))
                                          (meta levels))))
        levels-id (get-in strict (conj levels-strict-path :db/id))]
    (is (some? levels-id))
    (is (= (get-levels-id non-strict)
           levels-id))
    (is (= (get-levels-id updated)
           levels-id))
    (is (= (get-in back-to-strict (conj levels-strict-path :db/id))
           levels-id))
    (is (= (get-in without-new-levels (conj levels-strict-path :db/id))
           levels-id))
    (is (= (:db/id strict) (:db/id without-new-levels)))
    (is (= strict without-new-levels))))

(deftest test-set-class--round-trip
  (let [strict {:db/id 17592186055294, :orcpub.entity.strict/selections [{:db/id 17592186055298, :orcpub.entity.strict/key :class, :orcpub.entity.strict/options [{:db/id 17592186055299, :orcpub.entity.strict/key :barbarian, :orcpub.entity.strict/selections [{:db/id 17592186055300, :orcpub.entity.strict/key :levels, :orcpub.entity.strict/options [{:db/id 17592186055301, :orcpub.entity.strict/key :level-1}]}]}]} {:db/id 17592186055302, :orcpub.entity.strict/key :weapons, :orcpub.entity.strict/options [{:db/id 17592186055303, :orcpub.entity.strict/key :javelin, :orcpub.entity.strict/map-value {:db/id 17592186055304, :orcpub.dnd.e5.character.equipment/quantity 4, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]} {:db/id 17592186055305, :orcpub.entity.strict/key :equipment, :orcpub.entity.strict/options [{:db/id 17592186055306, :orcpub.entity.strict/key :explorers-pack, :orcpub.entity.strict/map-value {:db/id 17592186055307, :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]}]}
        levels-id (get-in strict (conj levels-strict-path :db/id))
        classes-id (get-in strict [::se/selections 0 :db/id])
        non-strict (entity/from-strict strict)
        class-selection (some (fn [s] (if (= :class (::t/key s)) s)) (t5e/template ::t/selections))
        class-options (::t/options class-selection)
        class-option-map (zipmap (map ::t/key class-options) class-options)
        updated (-> non-strict
                    (eh/set-class [:set-class :bard 0 class-option-map])
                    (eh/set-class [:set-class :barbarian 0 class-option-map]))
        back-to-strict (entity/to-strict updated)
        without-equipment-ids (reduce
                               (fn [e i]
                                 (update-in e
                                            [::se/selections i ::se/options]
                                            (fn [os]
                                              (map
                                               (fn [o]
                                                 (-> o
                                                     (dissoc :db/id)
                                                     (update ::se/map-value dissoc :db/id)))
                                               os))))
                               strict
                               [1 2])]
    (is (some? class-selection))
    (is (some? classes-id))
    (is (= (get-classes-id non-strict) classes-id))
    (is (= (get-classes-id updated) classes-id))
    (is (= (get-levels-id non-strict) levels-id))
    (is (= (get-levels-id updated) levels-id))
    (is (= without-equipment-ids back-to-strict))))

(deftest test-add-starting-equipment--round-trip
  (let [strict {:db/id 17592186055309, :orcpub.entity.strict/values {:db/id 17592186055334, :orcpub.dnd.e5.character/custom-equipment [{:orcpub.dnd.e5.character.equipment/name "Map of city you grew up in", :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/background-starting-equipment? true} {:orcpub.dnd.e5.character.equipment/name "Pet mouse", :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/background-starting-equipment? true} {:orcpub.dnd.e5.character.equipment/name "Token to remember your parents", :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/background-starting-equipment? true}]}, :orcpub.entity.strict/selections [{:db/id 17592186055317, :orcpub.entity.strict/key :weapons, :orcpub.entity.strict/options [{:db/id 17592186055318, :orcpub.entity.strict/key :javelin, :orcpub.entity.strict/map-value {:db/id 17592186055319, :orcpub.dnd.e5.character.equipment/quantity 4, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]} {:db/id 17592186055320, :orcpub.entity.strict/key :equipment, :orcpub.entity.strict/options [{:db/id 17592186055321, :orcpub.entity.strict/key :explorers-pack, :orcpub.entity.strict/map-value {:db/id 17592186055322, :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}} {:orcpub.entity.strict/key :knife-small, :orcpub.entity.strict/map-value {:orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/background-starting-equipment? true}} {:orcpub.entity.strict/key :clothes-common, :orcpub.entity.strict/map-value {:orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/background-starting-equipment? true}} {:orcpub.entity.strict/key :pouch, :orcpub.entity.strict/map-value {:orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/background-starting-equipment? true}}]} {:db/id 17592186055329, :orcpub.entity.strict/key :background, :orcpub.entity.strict/option {:db/id 17592186055330, :orcpub.entity.strict/key :urchin}} {:db/id 17592186055331, :orcpub.entity.strict/key :treasure, :orcpub.entity.strict/options [{:orcpub.entity.strict/key :gp, :orcpub.entity.strict/map-value {:orcpub.dnd.e5.character.equipment/quantity 10, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/background-starting-equipment? true}}]}]}
        non-strict (entity/from-strict strict)
        updated (-> non-strict
                    (eh/add-background-starting-equipment [:_ t5e/noble-bg])
                    (eh/add-background-starting-equipment [:_ t5e/urchin-bg]))
        back-to-strict (entity/to-strict updated)]
    (is (= strict back-to-strict))))

(deftest add-inventory-item--round-trip
  (let [strict {:db/id 17592186055339, :orcpub.entity.strict/selections [{:db/id 17592186055347, :orcpub.entity.strict/key :weapons, :orcpub.entity.strict/options [{:db/id 17592186055348, :orcpub.entity.strict/key :javelin, :orcpub.entity.strict/map-value {:db/id 17592186055349, :orcpub.dnd.e5.character.equipment/quantity 4, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]} {:db/id 17592186055350, :orcpub.entity.strict/key :equipment, :orcpub.entity.strict/options [{:db/id 17592186055351, :orcpub.entity.strict/key :explorers-pack, :orcpub.entity.strict/map-value {:db/id 17592186055352, :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]}]}
        non-strict (entity/from-strict strict)
        updated (-> non-strict
                    (eh/add-inventory-item [:_ :weapons :dagger])
                    (eh/remove-inventory-item [:_ :weapons :dagger]))
        back-to-strict (entity/to-strict updated)]
    (is (= strict back-to-strict))))

(defn meta-path [entity-path entity]
  (let [paths (reductions
               conj
               []
               entity-path)]
    (map #(meta (get-in entity %)) paths)))

(deftest update-single-select--round-trip
  (let [strict {:db/id 17592186055354, :orcpub.entity.strict/selections [{:db/id 17592186055371, :orcpub.entity.strict/key :race, :orcpub.entity.strict/option {:orcpub.entity.strict/key :elf}}]}
        option-id (get-in strict [::se/selections 0 :db/id])
        non-strict (entity/from-strict strict)
        updated (-> non-strict
                    (eh/select-option [:_
                                       {:option-path [:race]
                                        :selected? false
                                        :selectable? true
                                        :meets-prereqs? true
                                        :has-selections? true
                                        :built-template t5e/template
                                        :new-option-path [:race :dwarf]
                                        :selection {::t/multiselect? false ::t/min 1 ::t/max 1}
                                        :option {::t/key :dwarf}}])
                    (eh/select-option [:_
                                       {:option-path [:race]
                                        :selected? false
                                        :selectable? true
                                        :meets-prereqs? true
                                        :has-selections? true
                                        :built-template t5e/template
                                        :new-option-path [:race :elf]
                                        :selection {::t/multiselect? false ::t/min 1 ::t/max 1}
                                        :option {::t/key :elf}}]))
        back-to-strict (entity/to-strict updated)]
    (is (= option-id (get-race-id non-strict)))
    (is (= strict back-to-strict))))

(deftest update-multi-select--round-trip
  (let [strict {:db/id 17592186055379, :orcpub.entity.strict/selections [{:db/id 17592186055393, :orcpub.entity.strict/key :skill-profs, :orcpub.entity.strict/options [{:orcpub.entity.strict/key :arcana}]}]}
        skills-id (get-in strict [::se/selections 0 :db/id])
        non-strict (entity/from-strict strict)
        without-arcana-with-deception
        (-> non-strict
            (eh/select-option [:_
                               {:option-path [:skill-profs]
                                :selected? false
                                :selectable? true
                                :meets-prereqs? true
                                :has-selections? true
                                :built-template t5e/template
                                :new-option-path [:skill-profs :arcana]
                                :selection {::t/multiselect? true ::t/min 1 ::t/max 1}
                                :option {::t/key :arcana}}])
            (eh/select-option [:_
                               {:option-path [:skill-profs]
                                :selected? true
                                :selectable? true
                                :meets-prereqs? true
                                :has-selections? true
                                :built-template t5e/template
                                :new-option-path [:skill-profs :deception]
                                :selection {::t/multiselect? true ::t/min 1 ::t/max 1}
                                :option {::t/key :deception}}]))
        change-back
        (-> without-arcana-with-deception
            (eh/select-option [:_
                               {:option-path [:skill-profs]
                                :selected? true
                                :selectable? true
                                :meets-prereqs? true
                                :has-selections? true
                                :built-template t5e/template
                                :new-option-path [:skill-profs :deception]
                                :selection {::t/multiselect? true ::t/min 1 ::t/max 1}
                                :option {::t/key :deception}}])
            (eh/select-option [:_
                               {:option-path [:skill-profs]
                                :selected? false
                                :selectable? true
                                :meets-prereqs? true
                                :has-selections? true
                                :built-template t5e/template
                                :new-option-path [:skill-profs :arcana]
                                :selection {::t/multiselect? true ::t/min 1 ::t/max 1}
                                :option {::t/key :arcana}}]))
        back-to-strict (entity/to-strict change-back)]
    (is (= skills-id (get-skills-id non-strict)))
    (is (= strict back-to-strict))))
